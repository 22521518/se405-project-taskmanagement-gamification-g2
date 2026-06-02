package com.example.se405.backend

import com.example.se405.backend.database.model.BuiltinLabelSeeds
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.repository.HabitLabelRepository
import com.example.se405.backend.database.repository.UserRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

/**
 * Seeds required reference data on startup:
 *  1. 7 builtin HabitLabels (stable UUIDs matching frontend BuiltinLabels)
 *  2. A default "dev" fake user for local testing, matching the fake user
 *     defined in the Android frontend's __test_data__ directory.
 *
 * Uses "upsert" pattern (saveIfAbsent) so it is idempotent across restarts.
 */
@Component
class DataInitializer(
    private val habitLabelRepository: HabitLabelRepository,
    private val userRepository: UserRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        backfillUserPresence()
        seedHabitLabels()
        seedFakeUser()
    }

    /**
     * Fixes legacy `users.is_online` NULLs introduced when the column was added
     * via ddl-auto=update. Must run before any code hydrates UserEntity, since a
     * NULL would crash the non-nullable Kotlin `Boolean` property (root cause of
     * the HTTP 400 on login). Runs first via a native UPDATE so it never loads
     * the broken rows.
     */
    private fun backfillUserPresence() {
        userRepository.backfillNullIsOnline()
    }

    private fun seedHabitLabels() {
        BuiltinLabelSeeds.all.forEach { label ->
            if (!habitLabelRepository.existsById(label.uuid)) {
                habitLabelRepository.save(label)
            }
        }
    }

    private fun seedFakeUser() {
        val fakeUserId = FakeUserSeed.UUID
        if (!userRepository.existsById(fakeUserId)) {
            userRepository.save(
                UserEntity(
                    uuid = fakeUserId,
                    email = FakeUserSeed.EMAIL,
                    username = FakeUserSeed.USERNAME,
                    passwordHash = FakeUserSeed.PASSWORD_HASH,
                    displayName = FakeUserSeed.DISPLAY_NAME,
                    avatarUrl = null,
                    createdAt = LocalDateTime.of(2025, 1, 1, 0, 0),
                    updatedAt = LocalDateTime.of(2025, 1, 1, 0, 0),
                )
            )
        }
    }
}

/**
 * Stable seed values for the default fake user.
 * UUID must match FakeUser.kt in the Android frontend's __test_data__ directory.
 */
object FakeUserSeed {
    val UUID: UUID = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")
    const val EMAIL = "dev@example.com"
    const val USERNAME = "dev_user"
    const val PASSWORD_HASH = "fake_hash_for_dev"
    const val DISPLAY_NAME = "Dev User"
}
