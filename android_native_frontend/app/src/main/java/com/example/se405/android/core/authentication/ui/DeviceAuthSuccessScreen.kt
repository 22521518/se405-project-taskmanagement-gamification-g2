package com.example.se405.android.core.authentication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.authentication.BiometricViewmodel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DeviceAuthSuccessScreen(
    onLogout: () -> Unit,
    viewModel: BiometricViewmodel = koinViewModel()
) {
    DeviceAuthSuccessScreenContent(
        onLogout = {
            viewModel.logout()
            onLogout()
        }
    )
}

@Composable
fun DeviceAuthSuccessScreenContent(
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome, Guest!",
            style = AppText.HeadBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "You have authenticated as the device owner.",
            style = AppText.BodyRegular,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        ButtonApp(
            onClick = onLogout,
            type = ButtonType.FILLED,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(text = "Logout", style = AppText.BodyBold)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DeviceAuthSuccessScreenPreview() {
    com.example.se405.android.core.presentation.theme.Android_Theme {
        DeviceAuthSuccessScreenContent(onLogout = {})
    }
}
