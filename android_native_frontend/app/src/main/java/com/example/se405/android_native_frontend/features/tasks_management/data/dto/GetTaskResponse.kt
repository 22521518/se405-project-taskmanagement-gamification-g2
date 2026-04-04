package com.example.se405.android_native_frontend.features.tasks_management.data.dto

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android_native_frontend.core.presentation.components.HabitLabel
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskType
import com.example.se405.android_native_frontend.features.users_management.domain.entity.User
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class GetTaskResponse@OptIn(ExperimentalUuidApi::class)
constructor(
    val uuid: Uuid,
    val title: String,
    val description: String,
    val repetition: Int,

    val type: TaskType,
    val status: TaskStatus,
    val priority: TaskPriority,

    val assigneeId: Uuid,
    val creatorId: Uuid,
    val projectId: Uuid?,

    val startDate: LocalDate,
    val dueDate: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,

    // relation 1-n
    val creator: User?,
    val assignee: User?,
//    val project: Project?,

    // relations n-n
    val tagIds: List<Uuid>,
    val habitLabelIds: List<Uuid>,
    val taskCompletionLogIds: List<Uuid>,

    val tags: List<Tag>,
    val habitLabels: List<HabitLabel>,
    val taskCompletionLog: List<TaskCompletionLog>,
)