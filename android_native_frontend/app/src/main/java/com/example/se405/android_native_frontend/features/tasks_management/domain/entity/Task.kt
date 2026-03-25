package com.example.se405.android_native_frontend.features.tasks_management.domain.entity

import com.example.se405.android_native_frontend.features.users_management.domain.entity.User
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Task@OptIn(ExperimentalUuidApi::class)
constructor(
    val uuid: Uuid,
    val title: String,
    val description: String,
    val repetition: Int,
    val type: TaskType,
    val status: TaskStatus,
    val priority: TaskPriority,
    val creator: User?,
    val tags: List<Tag>,
    val taskCompletionLog: List<TaskCompletionLog>,

    val startDate: LocalDate,
    val dueDate: LocalDate,
    )

enum class TaskType { PROJECT, HABIT }
enum class TaskStatus {  TODO, IN_PROGRESS, DONE, CANCELLED }
enum class TaskPriority { LOW, MEDIUM, HIGH }