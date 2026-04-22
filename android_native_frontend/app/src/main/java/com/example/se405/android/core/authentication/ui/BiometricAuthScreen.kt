package com.example.se405.android.core.authentication.ui

import com.example.se405.android.core.authentication.BiometricViewmodel

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.se405.android.core.presentation.theme.AppText
import org.koin.androidx.compose.koinViewModel

private const val TAG = "BiometricAuthScreen"

@Composable
fun BiometricAuthScreen(
    onAuthenticated: () -> Unit,
    onGuestAuthenticated: () -> Unit,
    viewModel: BiometricViewmodel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // Redirection after success
    LaunchedEffect(viewModel.isAuthenticated) {
        if (viewModel.isAuthenticated) {
            onAuthenticated()
        }
    }

    LaunchedEffect(viewModel.isGuestAuthenticated) {
        if (viewModel.isGuestAuthenticated) {
            onGuestAuthenticated()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Log.i(TAG, "[toastEvent] $message")
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BiometricAuth(
            onLogin = viewModel::loginWithAccount,
            onRegister = viewModel::register,
            onBiometricClick = { isAccountMode ->
                activity?.let {
                    if (isAccountMode) {
                        viewModel.loginWithBiometric(it)
                    } else {
                        viewModel.authAsGuest(it)
                    }
                } ?: run {
                    Log.e(TAG, "[onBiometricClick] FragmentActivity not found")
                    Toast.makeText(context, "FragmentActivity not found", Toast.LENGTH_LONG).show()
                }
            }
        )

        // Enhanced UX: Show error message directly in UI with animation
        AnimatedVisibility(visible = viewModel.error != null) {
            viewModel.error?.let {
                Text(
                    text = it,
                    style = AppText.Body2Regular,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                )
            }
        }
        
        if (viewModel.isAuthenticating) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Authenticating...",
                style = AppText.CaptionRegular,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}