package com.example.se405.android.core.authentication.managers

import androidx.fragment.app.FragmentActivity

/**
 * Handles Mode 1 (Device Ownership Auth).
 * Stateless authentication without KeyStore or CryptoObject.
 */
class DeviceAuthManager {
    fun authenticateGuest(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit,
        onError: (String) -> Unit
    ) {
        val prompt = createBiometricPrompt(
            fragmentActivity = activity,
            onSuccess = { onSuccess() },
            onFailed = onFailed,
            onError = { _, errString -> onError(errString.toString()) }
        )

        val info = buildBiometricInfo(
            title = "Device Unlock",
            subTitle = "Verify device ownership",
            description = "Use your biometrics or PIN to access as guest",
            allowAuthenticators = true
        )

        authWithBiometricPrompt(
            context = activity,
            onError = { _, errString -> onError(errString.toString()) },
            biometricPrompt = prompt,
            biometricInfo = info,
            cryptoObject = null // Crucial: No CryptoObject in Mode 1
        )
    }
}
