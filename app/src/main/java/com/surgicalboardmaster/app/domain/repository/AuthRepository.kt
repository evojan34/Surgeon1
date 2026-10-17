package com.surgicalboardmaster.app.domain.repository

import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.AuthenticatedUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    fun requireCurrentUserId(): String
    suspend fun signInWithEmail(email: String, password: String): Result<AuthenticatedUser>
    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthenticatedUser>
    suspend fun continueInLocalWorkstationMode(displayName: String = "Board Candidate"): Result<AuthenticatedUser>
    suspend fun signOut()
}
