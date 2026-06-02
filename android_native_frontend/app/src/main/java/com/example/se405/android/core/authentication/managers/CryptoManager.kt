package com.example.se405.android.core.authentication.managers

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.*

class CryptoManager {

    companion object {
        private const val KEY_ALIAS = "biometric_key" // Unique alias for our key pair in the Android Keystore
                                                    // This demo only store one key pair (one device, one user)
                                                    // In a production app, consider using a more specific alias, possibly including user identifiers
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    fun getOrCreateKeyPair(): KeyPair {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKeyPair()
        }
        
        // This return Private Key Wrapper
        val privateKey = keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
        val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey
        
        if (privateKey == null || publicKey == null) {
            // If something went wrong, regenerate
            generateKeyPair()
            val freshKeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val freshPrivateKey = freshKeyStore.getKey(KEY_ALIAS, null) as PrivateKey
            val freshPublicKey = freshKeyStore.getCertificate(KEY_ALIAS).publicKey
            return KeyPair(freshPublicKey, freshPrivateKey)
        }
        
        return KeyPair(publicKey, privateKey)
    }

    private fun generateKeyPair() {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE
        )
        
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setUserAuthenticationRequired(true) // Requires biometric/PIN
            .build()
        
        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair() // Create keys in TEE or Secure Element
    }

    fun getPublicKeyBase64(): String {
        val pair = getOrCreateKeyPair()
        return Base64.encodeToString(pair.public.encoded, Base64.NO_WRAP)
    }

    fun sign(payload: String): String {
        val pair = getOrCreateKeyPair()
        
        val signature = Signature.getInstance("SHA256withRSA").apply {
            initSign(pair.private)
            update(payload.toByteArray())
        }
        
        return Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
    }

    fun getInitializedSignature(): Signature {
        val pair = getOrCreateKeyPair()
        
        return Signature.getInstance("SHA256withRSA").apply {
            initSign(pair.private)
        }
    }

    fun deleteKeys() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        keyStore.deleteEntry(KEY_ALIAS)
    }
}
