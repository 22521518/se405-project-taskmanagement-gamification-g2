package com.example.se405.android.core.authentication.managers

import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.security.Signature

/**
 * Handles Mode 2 (Account Biometric Unlock).
 * Uses KeyStore and CryptoObject to bind the session.
 */
class AccountBiometricManager(
    private val cryptoManager: CryptoManager
) {
    fun authenticateWithCrypto(
        activity: FragmentActivity,
        payload: String,
        title: String = "App Lock",
        subTitle: String = "Unlock using biometric",
        description: String = "Secure login to your account",
        onSuccess: (signatureBase64: String) -> Unit,
        onFailed: () -> Unit,
        onError: (String) -> Unit,
        onKeyInvalidated: () -> Unit
    ) {
        val signature: Signature
        try {
            signature = cryptoManager.getInitializedSignature()
        } catch (e: KeyPermanentlyInvalidatedException) {
            onKeyInvalidated()
            return
        } catch (e: Exception) {
            // Note: If no key exists, or other crypto error, handle it.
            onError("Crypto initialization failed: ${e.message}")
            return
        }

        val cryptoObject = BiometricPrompt.CryptoObject(signature)

        val prompt = createBiometricPrompt(
            fragmentActivity = activity,
            onSuccess = { result ->
                try {
                    val sig = result.cryptoObject?.signature
                    if (sig != null) {
                        sig.update(payload.toByteArray())
                        val sigBytes = sig.sign()
                        val signatureBase64 = android.util.Base64.encodeToString(sigBytes, android.util.Base64.NO_WRAP)
                        onSuccess(signatureBase64)
                    } else {
                        onError("CryptoObject signature was null")
                    }
                } catch (e: Exception) {
                    onError("Signing failed: ${e.message}")
                }
            },
            onFailed = onFailed,
            onError = { _, errString -> onError(errString.toString()) }
        )

        val info = buildBiometricInfo(
            title = title,
            subTitle = subTitle,
            description = description,
            allowAuthenticators = false // Mode 2: strictly biometrics when CryptoObject is involved normally, though DEVICE_CREDENTIAL sometimes allowed on API 30+ but we follow standard practice. Let buildBiometricInfo determine it. Wait, buildBiometricInfo hardcodes it to false if we pass allowAuthenticators=false.
        )

        authWithBiometricPrompt(
            context = activity,
            onError = { _, errString -> onError(errString.toString()) },
            biometricPrompt = prompt,
            biometricInfo = info,
            cryptoObject = cryptoObject // Crucial for Mode 2
        )
    }
}
