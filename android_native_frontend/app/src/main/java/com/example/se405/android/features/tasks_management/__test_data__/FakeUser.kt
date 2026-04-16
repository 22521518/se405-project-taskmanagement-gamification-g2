@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.__test_data__

import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A stable fake user for local development and preview/testing.
 *
 * The UUID is fixed (00000000-0000-0000-0000-000000000001) and must match
 * FakeUserSeed in the backend's DataInitializer.kt.
 * The backend will automatically create this user in the DB on first startup.
 */
object FakeUser {
    val UUID: Uuid = Uuid.fromLongs(0L, 1L)

    val instance = User(
        uuid = UUID,
        email = "dev@example.com",
        username = "dev_user",
        passwordHash = "fake_hash_for_dev",
        displayName = "Dev User",
        avatarUrl = "",
        createdAt = LocalDateTime.of(2025, 1, 1, 0, 0),
        updatedAt = LocalDateTime.of(2025, 1, 1, 0, 0),
    )
}
