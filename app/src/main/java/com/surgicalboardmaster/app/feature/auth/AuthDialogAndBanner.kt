package com.surgicalboardmaster.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.surgicalboardmaster.app.core.ui.components.StatusBadge
import com.surgicalboardmaster.app.core.ui.theme.ClinicalSuccess
import com.surgicalboardmaster.app.core.ui.theme.ClinicalWarning
import com.surgicalboardmaster.app.domain.model.AuthState

@Composable
fun AuthAccountDialog(
    authState: AuthState,
    feedbackMessage: String?,
    onSignIn: (email: String, password: String) -> Unit,
    onRegister: (email: String, password: String, displayName: String) -> Unit,
    onContinueOffline: (displayName: String) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("Dr. Surgical Resident") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Surgical Workstation Account & Cloud Sync",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (authState) {
                    is AuthState.Authenticated -> {
                        StatusBadge(
                            text = if (authState.user.isCloudSynced) {
                                "Firebase Cloud Sync Active • UID: ${authState.user.uid.take(8)}"
                            } else {
                                "Local Offline Workstation • UID: ${authState.user.uid}"
                            },
                            color = if (authState.user.isCloudSynced) ClinicalSuccess else ClinicalWarning
                        )
                        Text(
                            text = "Active Profile: ${authState.user.displayName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is AuthState.Error -> {
                        Text(
                            text = authState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> Unit
                }

                if (!feedbackMessage.isNullOrBlank()) {
                    Text(
                        text = feedbackMessage,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isRegisterMode,
                        onClick = { isRegisterMode = false },
                        label = { Text("Cloud Sign In") }
                    )
                    FilterChip(
                        selected = isRegisterMode,
                        onClick = { isRegisterMode = true },
                        label = { Text("Register New Account") }
                    )
                }

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Candidate Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min 8 chars)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                onRegister(email, password, displayName)
                            } else {
                                onSignIn(email, password)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isRegisterMode) "Register" else "Sign In")
                    }

                    OutlinedButton(
                        onClick = { onContinueOffline(displayName) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Local Mode")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onSignOut) {
                Text("Reset / Sign Out")
            }
        }
    )
}
