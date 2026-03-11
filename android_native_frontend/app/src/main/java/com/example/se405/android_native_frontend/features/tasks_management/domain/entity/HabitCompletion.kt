package com.example.se405.android_native_frontend.features.tasks_management.domain.entity

import com.example.se405.android_native_frontend.features.users_management.domain.entity.User
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class HabitCompletion @OptIn(ExperimentalUuidApi::class)
constructor(
    val habitCompletionId: Uuid,
    val habitId: Uuid,
    val userId: Uuid,

    val date: LocalDate,            // the date to check the completion
    val completedAt: LocalDateTime, // used for the completion status

    val habit: Task,
    val user: User
)
