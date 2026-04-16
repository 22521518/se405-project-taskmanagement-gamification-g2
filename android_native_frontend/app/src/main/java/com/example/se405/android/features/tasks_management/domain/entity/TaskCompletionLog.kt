package com.example.se405.android.features.tasks_management.domain.entity

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class TaskCompletionLog @OptIn(ExperimentalUuidApi::class)
constructor(
    val taskCompletionId: Uuid,
    val status: TaskStatus, // the status of the task
    val date: LocalDate,            // the date to check the completion
    val completedAt: LocalDateTime, // used for the completion status

    val task: Uuid,
    val user: Uuid
)
