package com.example.se405.android_native_frontend.core.utils

import com.example.se405.android_native_frontend.features.users_management.domain.entity.User
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object PreviewUserData {
    val users = listOf(
        User(
            uuid = Uuid.random(),
            email = "alice@example.com",
            username = "alice_01",
            passwordHash = "bcrypt_hash_1_example",
            displayName = "Alice Nguyen",
            avatarUrl = "https://example.com/avatars/alice.png",
            createdAt = LocalDateTime.parse("2025-01-01T10:00:00"),
            updatedAt = LocalDateTime.parse("2025-01-01T10:00:00")
        ),
        User(
            uuid = Uuid.random(),
            email = "bob@example.com",
            username = "bob_dev",
            passwordHash = "bcrypt_hash_2_example",
            displayName = "Bob Tran",
            avatarUrl = "https://example.com/avatars/bob.png",
            createdAt = LocalDateTime.parse("2025-01-02T09:30:00"),
            updatedAt = LocalDateTime.parse("2025-01-02T09:30:00")
        ),
        User(
            uuid = Uuid.random(),
            email = "charlie@example.com",
            username = "charlie_88",
            passwordHash = "bcrypt_hash_3_example",
            displayName = "Charlie Pham",
            avatarUrl = "",
            createdAt = LocalDateTime.parse("2025-01-03T08:15:00"),
            updatedAt = LocalDateTime.parse("2025-01-03T08:20:00")
        ),
        User(
            uuid = Uuid.random(),
            email = "diana@example.com",
            username = "diana_k",
            passwordHash = "bcrypt_hash_4_example",
            displayName = "Diana Le",
            avatarUrl = "https://example.com/avatars/diana.jpg",
            createdAt = LocalDateTime.parse("2025-01-04T11:45:00"),
            updatedAt = LocalDateTime.parse("2025-01-04T12:00:00")
        ),
        User(
            uuid = Uuid.random(),
            email = "eric@example.com",
            username = "eric_tech",
            passwordHash = "bcrypt_hash_5_example",
            displayName = "Eric Vo",
            avatarUrl = "",
            createdAt = LocalDateTime.parse("2025-01-05T14:10:00"),
            updatedAt = LocalDateTime.parse("2025-01-05T14:10:00")
        )
    )
}