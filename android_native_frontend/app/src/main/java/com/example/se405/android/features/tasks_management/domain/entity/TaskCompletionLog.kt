package com.example.se405.android.features.tasks_management.domain.entity

import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class TaskCompletionLog @OptIn(ExperimentalUuidApi::class)
constructor(
    val taskCompletionId: Uuid?,
    val status: TaskStatus, // the status of the task
    val date: LocalDate?,            // the date to check the completion
    val completedAt: LocalDateTime?, // used for the completion status

    val taskId: Uuid?,
    val taskTitle: String? = null,
    val userId: Uuid?,
    val userDisplayName: String? = null,
)
