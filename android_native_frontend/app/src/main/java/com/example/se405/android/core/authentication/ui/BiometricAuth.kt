package com.example.se405.android.core.authentication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.TextFieldApp
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText

@Composable
fun BiometricAuth(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onBiometricClick: (Boolean) -> Unit,
) {
    var loginWithAccount by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    
    val textForBiometricBtn = if(loginWithAccount) "Login With Biometric" else "Authenticate for using phone"

    ColumnCenter(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        ColumnCenter {
            ColumnCenter(vertical = Arrangement.SpaceBetween) {
                Text("Use as guest?")
                Switch(
                    checked = loginWithAccount,
                    onCheckedChange = { loginWithAccount = it}
                )
            }
            if (loginWithAccount) {
                if (isRegisterMode) {
                    AccountRegisterAuth(
                        onRegister = onRegister,
                        onSwitchToLogin = { isRegisterMode = false }
                    )
                } else {
                    AccountPasswordAuth(
                        onLogin = onLogin,
                        onSwitchToRegister = { isRegisterMode = true }
                    )
                }
            } else {
                OwnerBiometricAuth()
            }

            if (!isRegisterMode) {
                ButtonApp(onClick = {onBiometricClick(loginWithAccount)}, type = ButtonType.OUTLINED) {
                    Text(text = "$textForBiometricBtn     ")
                    Icon(painterResource(id = R.drawable.icon_fingerprint), contentDescription = "Authenticate with biometric", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun OwnerBiometricAuth() {
    ColumnCenter(
        modifier = Modifier.padding(24.dp, 8.dp), vertical = Arrangement.spacedBy(16.dp)) {
        
        Icon(
            painter = painterResource(id = R.drawable.icon_fingerprint),
            contentDescription = "Device Owner",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Welcome, Device Owner",
            style = AppText.HeadSemiBold
        )
        
        Text(
            text = "Authenticate using your device biometrics to access without an account.",
            style = AppText.BodyRegular,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun AccountPasswordAuth(
    onLogin: (String, String) -> Unit,
    onSwitchToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("testuser") }
    var password by remember { mutableStateOf("123456") }

    var showPassword by remember { mutableStateOf(false) }
    ColumnCenter(
        modifier = Modifier.padding(24.dp, 8.dp), vertical = Arrangement.spacedBy(12.dp)) {
        TextFieldApp(labelTitle = "Username", value = username, onValueChange = {username = it}, maxLines = 1, maxTextLen = 20, style = AppText.BodyRegular)
        TextFieldApp(
            labelTitle = "Password",
            value = password,
            onValueChange = {password = it},
            maxLines = 1,
            maxTextLen = 20,
            style = AppText.BodyRegular,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
        )
        RowCenter(horizontal = Arrangement.SpaceBetween) {
            Text("Show password?")
            Switch(
                checked = showPassword,
                onCheckedChange = { showPassword = it}
            )
        }
        ButtonApp(onClick = { onLogin(username, password)}, type = ButtonType.FILLED) {
            Text(text = "Login With Account")
        }

        ButtonApp(onClick = onSwitchToRegister, type = ButtonType.TEXT) {
            Text(text = "Don't have an account? Register", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun AccountRegisterAuth(
    onRegister: (String, String, String, String) -> Unit,
    onSwitchToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("test@example.com") }
    var username by remember { mutableStateOf("testuser") }
    var password by remember { mutableStateOf("123456") }
    var displayName by remember { mutableStateOf("Test User") }
    var showPassword by remember { mutableStateOf(false) }

    ColumnCenter(
        modifier = Modifier.padding(24.dp, 8.dp), vertical = Arrangement.spacedBy(12.dp)) {
        Text("Create New Account", style = AppText.HeadSemiBold)
        
        TextFieldApp(labelTitle = "Email", value = email, onValueChange = {email = it}, maxLines = 1, maxTextLen = 40, style = AppText.BodyRegular)
        TextFieldApp(labelTitle = "Username", value = username, onValueChange = {username = it}, maxLines = 1, maxTextLen = 20, style = AppText.BodyRegular)
        TextFieldApp(labelTitle = "Display Name", value = displayName, onValueChange = {displayName = it}, maxLines = 1, maxTextLen = 30, style = AppText.BodyRegular)
        TextFieldApp(
            labelTitle = "Password",
            value = password,
            onValueChange = {password = it},
            maxLines = 1,
            maxTextLen = 20,
            style = AppText.BodyRegular,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
        )
        
        RowCenter(horizontal = Arrangement.SpaceBetween) {
            Text("Show password?")
            Switch(
                checked = showPassword,
                onCheckedChange = { showPassword = it}
            )
        }
        
        ButtonApp(onClick = { onRegister(email, username, password, displayName)}, type = ButtonType.FILLED) {
            Text(text = "Register Account")
        }

        ButtonApp(onClick = onSwitchToLogin, type = ButtonType.TEXT) {
            Text(text = "Already have an account? Login", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun ColumnCenter(modifier: Modifier = Modifier, horizontal: Alignment.Horizontal = Alignment.CenterHorizontally, vertical: Arrangement.Vertical = Arrangement.Center, children: @Composable () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontal,
        verticalArrangement = vertical
    ) { children() }
}

@Composable
private fun RowCenter(modifier: Modifier = Modifier.fillMaxWidth(), vertical: Alignment.Vertical = Alignment.CenterVertically, horizontal: Arrangement.Horizontal = Arrangement.Center, children: @Composable () -> Unit) {
    Row(
        modifier = modifier,
        verticalAlignment = vertical,
        horizontalArrangement = horizontal
    ) { children() }
}


@Preview(showBackground = true, backgroundColor = 0xFFECF7FB)
@Composable
fun BiometricAuthPreview() {
    Android_Theme {
        BiometricAuth({_, _ ->

        }, {_, _, _, _ ->

        }, {})
    }
}