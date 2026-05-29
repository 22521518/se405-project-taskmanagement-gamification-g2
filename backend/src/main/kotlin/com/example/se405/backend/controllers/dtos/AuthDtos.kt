package com.example.se405.backend.controllers.dtos

import java.util.UUID

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val displayName: String
)

data class LoginRequest(
    val username: String,
    val password: String,
    val deviceId: String? = null
)

data class BiometricEnableRequest(
    val deviceId: String,
    val publicKey: String
)

data class BiometricLoginRequest(
    val deviceId: String,
    val payload: String,
    val signature: String
)

data class UpdateProfileRequest(
    val displayName: String?,
    val email: String?,
    val avatarUrl: String?
)

data class AuthResponse(
    val token: String,
    val userId: UUID,
    val username: String,
    val displayName: String,
    val biometricEnabled: Boolean
)

data class SimpleMessageResponse(
    val message: String
)

data class MessagePayload(
    val uuid: String,
    val content: String,
    val type: String,
    val fileUrl: String?,
    val fileName: String?,
    val fileSize: String?,
    val createdAt: String,
    val conversationId: String,
    val sender: UserPayload,
    val replyTo: MessagePayload? = null
)

data class UserPayload(
    val uuid: String,
    val displayName: String?,
    val avatarUrl: String?,
    val email: String?
)

data class UserProfileResponse(
    val uuid: UUID,
    val email: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?
)
