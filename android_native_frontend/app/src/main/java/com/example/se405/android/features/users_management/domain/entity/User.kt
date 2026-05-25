package com.example.se405.android.features.users_management.domain.entity

import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class User @OptIn(ExperimentalUuidApi::class)
constructor(
    val uuid: Uuid,
    val email: String,
    val username: String,
    val passwordHash: String?,
    val displayName: String,
    val avatarUrl: String?,

    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    init {
        require(email.isNotBlank()) { "Email must not be blank" }
        require(email.length <= 254) { "Email too long" }
        require(EMAIL_REGEX.matches(email)) { "Invalid email format" }

        require(username.length in 3..50) { "Username must be 3..50 characters" }
        require(USERNAME_REGEX.matches(username)) {
            "Username can contain only letters, numbers, underscore"
        }

//        require(passwordHash.isNotBlank()) { "Password hash must not be blank" }
//        require(password_hash.length >= 60) { "Invalid password hash length" }
//        // ví dụ bcrypt thường ~60 chars

        require(displayName.isNotBlank()) { "Display name must not be blank" }
        require(displayName.length in 3..50) { "Display name must be 3..50 characters" }

        if (avatarUrl != null)
            require(avatarUrl.isNotBlank() && URL_REGEX.matches(avatarUrl)) {
                "Avatar URL must be valid"
            }

        require(!updatedAt.isBefore(createdAt)) {
            "updated_at must be after created_at"
        }
    }

    companion object {
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

        private val URL_REGEX =
            Regex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$")
        private val USERNAME_REGEX =
            Regex("^[a-zA-Z0-9_]+$")
    }
}