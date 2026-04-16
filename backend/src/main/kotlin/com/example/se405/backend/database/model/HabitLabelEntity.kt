package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.util.UUID

/**
 * Represents a habit label category for tags.
 * Seeded with 7 built-in labels matching the Android frontend's BuiltinLabels list.
 * UUIDs use fromLongs(n, n) pattern to be stable and predictable.
 */
@Entity
@Table(name = "habit_labels")
data class HabitLabelEntity(
    @Id
    val uuid: UUID,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val description: String,
)

/**
 * Stable seed UUIDs for builtin labels — must match frontend's Uuid.fromLongs(n, n).
 * UUID(mostSigBits, leastSigBits) maps to Kotlin's Uuid.fromLongs(high, low).
 */
object BuiltinLabelSeeds {
    val WORK        = UUID(1L, 1L)  // "Work"
    val HEALTH      = UUID(2L, 2L)  // "Health"
    val PERSONAL    = UUID(3L, 3L)  // "Personal"
    val LEARNING    = UUID(4L, 4L)  // "Learning"
    val BAD         = UUID(5L, 5L)  // "Bad"
    val CREATIVE    = UUID(6L, 6L)  // "Creative"
    val NETWORKING  = UUID(7L, 7L)  // "Networking"

    val all = listOf(
        HabitLabelEntity(WORK,       "Work",       "Career, job-related tasks, and income-generating activities"),
        HabitLabelEntity(HEALTH,     "Health",     "Physical health, mental well-being, and self-care"),
        HabitLabelEntity(PERSONAL,   "Personal",   "Daily life, family, and personal responsibilities"),
        HabitLabelEntity(LEARNING,   "Learning",   "Studying, skill development, and knowledge growth"),
        HabitLabelEntity(BAD,        "Bad",        "Bad habits that have a negative impact on your life"),
        HabitLabelEntity(CREATIVE,   "Creative",   "Creative work such as writing, design, and side projects"),
        HabitLabelEntity(NETWORKING, "Networking", "Social activities and interactions with others"),
    )
}
