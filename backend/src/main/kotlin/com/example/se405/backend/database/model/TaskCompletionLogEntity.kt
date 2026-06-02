package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Records a task completion or "won't do" action for a given date.
 * - date: the calendar date being tracked (habit repetition date)
 * - completedAt: timestamp when the user marked it done/won't-do
 * - taskId: references the parent Task
 * - userId: references the user who performed the action
 */
@Entity
@Table(name = "task_completion_logs")
data class TaskCompletionLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val taskCompletionId: UUID? = null,

    @Column(name = "task_id", nullable = false)
    val taskId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "status")
    val status: String = "TODO",

    @Column(nullable = false)
    val date: LocalDate,

    @Column(name = "completed_at", nullable = false)
    val completedAt: LocalDateTime,
)
