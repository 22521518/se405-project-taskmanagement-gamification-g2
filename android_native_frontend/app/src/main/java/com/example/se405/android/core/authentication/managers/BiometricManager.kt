package com.example.se405.android.core.authentication.managers

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

fun authWithBiometricPrompt(
    context: Context,
    onError: (errorCode: Int, errString: CharSequence) -> Unit,
    biometricPrompt: BiometricPrompt,
    biometricInfo: BiometricPrompt.PromptInfo = buildBiometricInfo(),
    cryptoObject: BiometricPrompt.CryptoObject? = null
) {
    when(checkBiometricAuthAvailable(context)) {
        BiometricAuthStatus.NOT_AVAILABLE -> {
            onError(BiometricAuthStatus.NOT_AVAILABLE.code, "Not available for this device")
            return
        }
        BiometricAuthStatus.TEMPORARY_NOT_AVAILABLE -> {
            onError(BiometricAuthStatus.TEMPORARY_NOT_AVAILABLE.code, "Not available at this moment")
            return
        }
        BiometricAuthStatus.AVAILABLE_BUT_NOT_ENROLLED -> {
            onError(BiometricAuthStatus.AVAILABLE_BUT_NOT_ENROLLED.code, "You should add a fingerprint or face id first")
            return
        }
        else -> Unit
    }

    if (cryptoObject != null) {
        biometricPrompt.authenticate(biometricInfo, cryptoObject)
    } else {
        biometricPrompt.authenticate(biometricInfo)
    }
}


fun checkBiometricAuthAvailable(context: Context): BiometricAuthStatus {
    val biometricManager = BiometricManager.from(context.applicationContext)
    return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAuthStatus.READY
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAuthStatus.NOT_AVAILABLE
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAuthStatus.TEMPORARY_NOT_AVAILABLE
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAuthStatus.AVAILABLE_BUT_NOT_ENROLLED
        else -> BiometricAuthStatus.NOT_AVAILABLE
    }
}

fun createBiometricPrompt(
    fragmentActivity: FragmentActivity,
    onSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit,
    onFailed: () -> Unit,
    onError: (errorCode: Int, errString: CharSequence) -> Unit
): BiometricPrompt {
    return BiometricPrompt(
        fragmentActivity,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(result)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        })
}

fun buildBiometricInfo(
    title: String = "Sample App Authentication",
    subTitle: String = "Please login to get access",
    description: String = "Sample App is using Android biometric authentication",
    allowAuthenticators: Boolean = false,
    negativeButtonText: String = "Cancel"
): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder().apply {
        setTitle(title)
        setSubtitle(subTitle)
        setDescription(description)
        setConfirmationRequired(false)
        if (allowAuthenticators) setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        else setNegativeButtonText(negativeButtonText)
    }.build()
}

enum class BiometricAuthStatus(val code: Int) {
    READY(1),                       // We can interact with the biometric support
    NOT_AVAILABLE(-1),              //  Biometry support not present
    TEMPORARY_NOT_AVAILABLE(-2),    // Biometric support is currently unavailable
    AVAILABLE_BUT_NOT_ENROLLED(-3), // Biometric support is available, but no biometry has enrolled
}