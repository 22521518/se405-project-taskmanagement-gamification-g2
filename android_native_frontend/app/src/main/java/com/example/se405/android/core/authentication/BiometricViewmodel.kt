package com.example.se405.android.core.authentication

import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.data.AuthRepository
import com.example.se405.android.core.authentication.data.BiometricEnableRequest
import com.example.se405.android.core.authentication.data.BiometricLoginRequest
import com.example.se405.android.core.authentication.data.LoginRequest
import com.example.se405.android.core.authentication.managers.AccountBiometricManager
import com.example.se405.android.core.authentication.managers.CryptoManager
import com.example.se405.android.core.authentication.managers.DeviceAuthManager
import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BiometricViewmodel(
    private val repository: AuthRepository,
    private val prefs: AuthPreferences,
    private val cryptoManager: CryptoManager,
    private val deviceAuthManager: DeviceAuthManager,
    private val accountBiometricManager: AccountBiometricManager,
    private val application: android.app.Application
): ViewModel() {

    companion object {
        private const val TAG = "BiometricViewmodel"
    }

    var isAuthenticated by mutableStateOf(false)
        private set
        
    var isGuestAuthenticated by mutableStateOf(false)
        private set

    var isBiometricEnabled by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var isAuthenticating by mutableStateOf(false)
        private set
        
    var username by mutableStateOf<String?>(null)
        private set
        
    var displayName by mutableStateOf<String?>(null)
        private set

    private val _toastEvent = Channel<String>()
    val toastEvent = _toastEvent.receiveAsFlow()

    private val deviceId = Settings.Secure.getString(
        application.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val token = prefs.authToken.first()
            if (token != null) {
                isAuthenticated = true
            }
            isBiometricEnabled = prefs.isBiometricEnabled.first()
            username = prefs.username.first()
            displayName = prefs.displayName.first()
        }
    }

    // MODE 1 EVENT
    fun authAsGuest(fragmentActivity: FragmentActivity) {
        isAuthenticating = true
        error = null

        deviceAuthManager.authenticateGuest(
            activity = fragmentActivity,
            onSuccess = {
                Log.i(TAG, "[authAsGuest] Guest authentication successful")
                isGuestAuthenticated = true
                isAuthenticating = false
            },
            onFailed = {
                Log.w(TAG, "[authAsGuest] Guest authentication failed")
                showError("Guest authentication failed")
                isAuthenticating = false
            },
            onError = { err ->
                Log.e(TAG, "[authAsGuest] Error: $err")
                showError(err)
                isAuthenticating = false
            }
        )
    }

    // MODE 2 EVENTS
    fun loginWithAccount(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            showError("Please fill all fields")
            return
        }

        isAuthenticating = true
        error = null

        viewModelScope.launch {
            Log.d(TAG, "[loginWithAccount] Attempting login for user: $username, deviceId: $deviceId")
            val result = repository.login(LoginRequest(username, password, deviceId))
            result.onSuccess { response ->
                Log.i(TAG, "[loginWithAccount] Login successful. userId=${response.userId}, biometricEnabled=${response.biometricEnabled}")
                prefs.saveAuth(response.token, response.userId, response.username, response.displayName, response.biometricEnabled)
                this@BiometricViewmodel.username = response.username
                this@BiometricViewmodel.displayName = response.displayName
                isAuthenticated = true
                isAuthenticating = false
            }.onFailure { e ->
                Log.e(TAG, "[loginWithAccount] Login failed", e)
                showError(e.message ?: "Login failed")
                isAuthenticating = false
            }
        }
    }

    fun register(email: String, username: String, password: String, displayName: String) {
        if (email.isBlank() || username.isBlank() || password.isBlank() || displayName.isBlank()) {
            showError("Please fill all fields")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Invalid email format")
            return
        }

        if (password.length < 6) {
            showError("Password must be at least 6 characters")
            return
        }

        isAuthenticating = true
        error = null

        viewModelScope.launch {
            Log.d(TAG, "[register] Attempting registration for user: $username, email: $email, deviceId: $deviceId")
            val result = repository.register(com.example.se405.android.core.authentication.data.RegisterRequest(email, username, password, displayName))
            result.onSuccess { response ->
                Log.i(TAG, "[register] Registration successful. userId=${response.userId}")
                prefs.saveAuth(response.token, response.userId, response.username, response.displayName, response.biometricEnabled)
                this@BiometricViewmodel.username = response.username
                this@BiometricViewmodel.displayName = response.displayName
                isAuthenticated = true
                isAuthenticating = false
            }.onFailure { e ->
                Log.e(TAG, "[register] Registration failed", e)
                showError(e.message ?: "Registration failed")
                isAuthenticating = false
            }
        }
    }


    fun loginWithBiometric(fragmentActivity: FragmentActivity) {
        isAuthenticating = true
        error = null
        
        val payload = "login_${System.currentTimeMillis()}"

        accountBiometricManager.authenticateWithCrypto(
            activity = fragmentActivity,
            payload = payload,
            onSuccess = { signatureBase64 ->
                viewModelScope.launch {
                    Log.d(TAG, "[loginWithBiometric] Biometric prompt passed. Sending signature to backend. deviceId=$deviceId")
                    val result = repository.loginBiometric(BiometricLoginRequest(deviceId, payload, signatureBase64))
                    
                    result.onSuccess { response ->
                        Log.i(TAG, "[loginWithBiometric] Backend verification successful. userId=${response.userId}")
                        prefs.saveAuth(response.token, response.userId, response.username, response.displayName, response.biometricEnabled)
                        username = response.username
                        displayName = response.displayName
                        isAuthenticated = true
                        isAuthenticating = false
                    }.onFailure { e ->
                        Log.e(TAG, "[loginWithBiometric] Backend verification failed", e)
                        showError("Server verification failed: ${e.message}")
                        isAuthenticating = false
                    }
                }
            },
            onFailed = {
                Log.w(TAG, "[loginWithBiometric] Biometric prompt failed (user action)")
                showError("Biometric authentication failed")
                isAuthenticating = false
            },
            onError = { err ->
                Log.e(TAG, "[loginWithBiometric] Biometric error: $err")
                showError(err)
                isAuthenticating = false
            },
            onKeyInvalidated = {
                Log.w(TAG, "[loginWithBiometric] Key permanently invalidated")
                handleKeyInvalidated()
            }
        )
    }

    fun logout() {
        viewModelScope.launch {
            prefs.clearAuth()
            isAuthenticated = false
            isGuestAuthenticated = false
        }
    }

    fun toggleBiometric(fragmentActivity: FragmentActivity, enabled: Boolean) {
        if (enabled) {
            enableBiometricReal(fragmentActivity)
        } else {
            disableBiometricReal()
        }
    }

    private fun enableBiometricReal(fragmentActivity: FragmentActivity) {
        isAuthenticating = true
        
        val payload = "setup_${System.currentTimeMillis()}"

        accountBiometricManager.authenticateWithCrypto(
            activity = fragmentActivity,
            payload = payload,
            title = "Enable Biometric",
            subTitle = "Confirm your identity",
            description = "Register this device for biometric login",
            onSuccess = { signatureBase64 ->
                viewModelScope.launch {
                    Log.d(TAG, "[enableBiometric] Biometric confirmed. Regenerating key pair and sending public key to backend. deviceId=$deviceId")
                    // Delete old keys first to ensure a fresh key pair
                    cryptoManager.deleteKeys()
                    val publicKey = cryptoManager.getPublicKeyBase64()
                    Log.d(TAG, "[enableBiometric] Fresh public key generated (${publicKey.length} chars)")
                    val result = repository.enableBiometric(BiometricEnableRequest(deviceId, publicKey))
                    result.onSuccess {
                        Log.i(TAG, "[enableBiometric] Biometric enabled on server successfully")
                        prefs.setBiometricEnabled(true)
                        isBiometricEnabled = true
                        isAuthenticating = false
                    }.onFailure { e ->
                        Log.e(TAG, "[enableBiometric] Failed to enable on server", e)
                        showError("Failed to enable biometric on server: ${e.message}")
                        isAuthenticating = false
                    }
                }
            },
            onFailed = {
                Log.w(TAG, "[enableBiometric] Biometric prompt failed")
                showError("Biometric authentication failed")
                isAuthenticating = false
            },
            onError = { err ->
                Log.e(TAG, "[enableBiometric] Biometric error: $err")
                showError(err)
                isAuthenticating = false
            },
            onKeyInvalidated = {
                Log.w(TAG, "[enableBiometric] Key permanently invalidated during enable flow")
                handleKeyInvalidated()
            }
        )
    }

    private fun disableBiometricReal() {
        viewModelScope.launch {
            Log.d(TAG, "[disableBiometric] Disabling biometric on server. deviceId=$deviceId")
            val result = repository.disableBiometric(deviceId)
            result.onSuccess {
                Log.i(TAG, "[disableBiometric] Biometric disabled successfully")
                prefs.setBiometricEnabled(false)
                isBiometricEnabled = false
                cryptoManager.deleteKeys()
            }.onFailure { e ->
                Log.e(TAG, "[disableBiometric] Failed to disable", e)
                showError("Failed to disable biometric: ${e.message}")
            }
        }
    }

    private fun handleKeyInvalidated() {
        // Automatically disable biometric when key is permanently invalidated
        showError("Biometric enrollment changed. Please login with password and re-enable biometric.")
        isAuthenticating = false
        viewModelScope.launch {
            prefs.setBiometricEnabled(false)
            isBiometricEnabled = false
            cryptoManager.deleteKeys()
            repository.disableBiometric(deviceId)
        }
    }

    private fun showError(msg: String) {
        Log.e(TAG, "[showError] $msg")
        error = msg
        viewModelScope.launch {
            _toastEvent.send(msg)
        }
    }

    fun resetState() {
        error = null
        isAuthenticating = false
    }
}