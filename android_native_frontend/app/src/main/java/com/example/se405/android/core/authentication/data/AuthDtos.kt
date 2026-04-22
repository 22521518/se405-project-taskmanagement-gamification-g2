package com.example.se405.android.core.authentication.data

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val displayName: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val deviceId: String? = null
)

@Serializable
data class BiometricEnableRequest(
    val deviceId: String,
    val publicKey: String
)

@Serializable
data class BiometricLoginRequest(
    val deviceId: String,
    val payload: String,
    val signature: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String,
    val displayName: String,
    val biometricEnabled: Boolean
)

@Serializable
data class SimpleMessageResponse(
    val message: String
)
