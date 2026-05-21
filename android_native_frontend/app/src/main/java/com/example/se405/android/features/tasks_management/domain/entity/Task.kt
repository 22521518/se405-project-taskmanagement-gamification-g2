package com.example.se405.android.features.tasks_management.domain.entity

import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Task(
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

    val startDate: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val projectId: Uuid? = null,
)

enum class TaskType { HABIT, PROJECT }
enum class TaskStatus {  TODO, IN_PROGRESS, DONE, FAILED }
enum class TaskPriority { LOW, MEDIUM, HIGH }