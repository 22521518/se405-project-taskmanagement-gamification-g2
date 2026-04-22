package com.example.se405.backend.services

import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Service
class RSAService {
    fun verify(payload: String, signature: String, publicKeyStr: String): Boolean {
        return try {
            val publicKeyBytes = Base64.getDecoder().decode(publicKeyStr)
            val keySpec = X509EncodedKeySpec(publicKeyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey = keyFactory.generatePublic(keySpec)

            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(publicKey)
            sig.update(payload.toByteArray())
            
            sig.verify(Base64.getDecoder().decode(signature))
        } catch (e: Exception) {
            false
        }
    }
}
