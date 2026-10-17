package com.surgicalboardmaster.app.domain.model

data class AuthenticatedUser(
    val uid: String,
    val email: String?,
    val displayName: String,
    val isCloudSynced: Boolean
)

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: AuthenticatedUser) : AuthState
    data class Error(val message: String) : AuthState
}
