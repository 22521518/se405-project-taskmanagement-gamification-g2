package com.example.se405.android.core.authentication.ui

import com.example.se405.android.core.authentication.BiometricViewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.theme.AppText
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthSettingsScreen(
    onLogout: () -> Unit,
    onGoHome: () -> Unit,
    viewModel: BiometricViewmodel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    AuthSettingsContent(
        displayName = viewModel.displayName,
        username = viewModel.username,
        isBiometricEnabled = viewModel.isBiometricEnabled,
        error = viewModel.error,
        onToggleBiometric = { enabled ->
            activity?.let { act ->
                viewModel.toggleBiometric(act, enabled)
            }
        },
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        onGoHome = onGoHome
    )
}

@Composable
fun AuthSettingsContent(
    displayName: String?,
    username: String?,
    isBiometricEnabled: Boolean,
    error: String?,
    onToggleBiometric: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onGoHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // User Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial Placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (displayName ?: "U").take(1).uppercase(),
                        style = AppText.BodyBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = displayName ?: "User",
                        style = AppText.BodyBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "@${username ?: "unknown"}",
                        style = AppText.CaptionRegular,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Security Settings",
            style = AppText.HeadBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Biometric Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Biometric Login",
                        style = AppText.BodyBold
                    )
                    Text(
                        text = "Use fingerprint or face to unlock",
                        style = AppText.CaptionRegular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = onToggleBiometric
                )
            }
        }

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = AppText.CaptionRegular,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        ButtonApp(
            onClick = onGoHome,
            type = ButtonType.FILLED,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Home", style = AppText.BodyBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        ButtonApp(
            onClick = onLogout,
            type = ButtonType.OUTLINED,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Logout", style = AppText.BodyBold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AuthSettingsScreenPreview() {
    com.example.se405.android.core.presentation.theme.Android_Theme {
        AuthSettingsContent(
            displayName = "John Doe",
            username = "johndoe123",
            isBiometricEnabled = true,
            error = null,
            onToggleBiometric = {},
            onLogout = {},
            onGoHome = {}
        )
    }
}