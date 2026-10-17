package com.surgicalboardmaster.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository
        .observeAuthState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Loading
        )

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            _feedbackMessage.value = result.fold(
                onSuccess = { "Signed in as ${it.displayName} (Cloud Sync Active)" },
                onFailure = { it.message ?: "Authentication error" }
            )
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            val result = authRepository.registerWithEmail(email, password, displayName)
            _feedbackMessage.value = result.fold(
                onSuccess = { "Account created for ${it.displayName}" },
                onFailure = { it.message ?: "Registration error" }
            )
        }
    }

    fun switchToOfflineWorkstation(displayName: String) {
        viewModelScope.launch {
            authRepository.continueInLocalWorkstationMode(displayName)
            _feedbackMessage.value = "Switched to Local Workstation Mode ($displayName)"
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _feedbackMessage.value = "Signed out of cloud session."
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }
}
