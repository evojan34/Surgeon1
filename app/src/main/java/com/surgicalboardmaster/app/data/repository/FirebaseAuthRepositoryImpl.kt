package com.surgicalboardmaster.app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.AuthenticatedUser
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val defaultLocalUser = AuthenticatedUser(
        uid = "local_board_candidate",
        email = null,
        displayName = "Surgical Board Candidate (Offline Workstation)",
        isCloudSynced = false
    )

    private val authStateFlow = MutableStateFlow<AuthState>(
        AuthState.Authenticated(defaultLocalUser)
    )

    init {
        syncExistingFirebaseSessionIfAvailable()
    }

    private fun isFirebaseConfigured(): Boolean {
        return runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)
    }

    private fun syncExistingFirebaseSessionIfAvailable() {
        if (!isFirebaseConfigured()) return
        runCatching {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                authStateFlow.value = AuthState.Authenticated(
                    AuthenticatedUser(
                        uid = currentUser.uid,
                        email = currentUser.email,
                        displayName = currentUser.displayName ?: "Surgical Resident",
                        isCloudSynced = true
                    )
                )
            }
        }
    }

    override fun observeAuthState(): Flow<AuthState> = authStateFlow.asStateFlow()

    override fun requireCurrentUserId(): String {
        val currentState = authStateFlow.value
        return if (currentState is AuthState.Authenticated) {
            currentState.user.uid
        } else {
            defaultLocalUser.uid
        }
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<AuthenticatedUser> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("Password must be at least 8 characters."))
        }

        if (!isFirebaseConfigured()) {
            return Result.failure(
                IllegalStateException(
                    "Firebase google-services.json is not attached to this build. Use Local Workstation Mode or attach google-services.json."
                )
            )
        }

        return runCatching {
            val authResult = FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(trimmedEmail, password)
                .await()
            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Authentication succeeded but user record was null.")

            val authenticatedUser = AuthenticatedUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName ?: trimmedEmail.substringBefore("@"),
                isCloudSynced = true
            )
            authStateFlow.value = AuthState.Authenticated(authenticatedUser)
            authenticatedUser
        }.onFailure { error ->
            authStateFlow.value = AuthState.Error(error.message ?: "Sign-in failed.")
        }
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthenticatedUser> {
        val trimmedEmail = email.trim()
        val cleanName = displayName.trim()
        if (cleanName.length < 2) {
            return Result.failure(IllegalArgumentException("Display name must be at least 2 characters."))
        }
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("Password must be at least 8 characters."))
        }

        if (!isFirebaseConfigured()) {
            return Result.failure(
                IllegalStateException(
                    "Firebase google-services.json is not attached to this build. Use Local Workstation Mode or attach google-services.json."
                )
            )
        }

        return runCatching {
            val authResult = FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(trimmedEmail, password)
                .await()
            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Registration succeeded but user record was null.")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Initialize user root profile document with server timestamps per firestore.rules
            val userDoc = mapOf(
                "uid" to firebaseUser.uid,
                "displayName" to cleanName,
                "role" to "GENERAL_SURGERY_BOARD_CANDIDATE",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(firebaseUser.uid)
                .set(userDoc, SetOptions.merge())
                .await()

            val authenticatedUser = AuthenticatedUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = cleanName,
                isCloudSynced = true
            )
            authStateFlow.value = AuthState.Authenticated(authenticatedUser)
            authenticatedUser
        }.onFailure { error ->
            authStateFlow.value = AuthState.Error(error.message ?: "Registration failed.")
        }
    }

    override suspend fun continueInLocalWorkstationMode(
        displayName: String
    ): Result<AuthenticatedUser> {
        val cleanName = displayName.trim().ifBlank { "Surgical Board Candidate" }
        val user = defaultLocalUser.copy(displayName = cleanName)
        authStateFlow.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signOut() {
        if (isFirebaseConfigured()) {
            runCatching { FirebaseAuth.getInstance().signOut() }
        }
        authStateFlow.value = AuthState.Authenticated(defaultLocalUser)
    }
}
