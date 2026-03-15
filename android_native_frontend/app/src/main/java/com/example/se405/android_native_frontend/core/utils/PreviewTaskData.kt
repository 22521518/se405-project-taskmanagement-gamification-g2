@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.core.utils
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.HabitLabels
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskType
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object PreviewTaskData {

    // HabitLabels
    val habitLabelsList = listOf(
        HabitLabels(
            labelId = Uuid.random(),
            labelName = "Health",
            description = "Health related habits",
            icon = "ic_health",
            habitIds = emptyList(),
            habits = emptyList()
        ),
        HabitLabels(
            labelId = Uuid.random(),
            labelName = "Productivity",
            description = "Work and productivity habits",
            icon = "ic_productivity",
            habitIds = emptyList(),
            habits = emptyList()
        )
    )

    // Tags
    val tagsList = listOf(
        Tag(
            createdBy = PreviewUserData.users[0].uuid,
            taskIds = emptyList(),
            uuid = Uuid.random(),
            name = "Important",
            color = 0xFFFF0000.toInt(),
            label = habitLabelsList[1],
            createdAt = LocalDateTime.parse("2025-01-01T10:00:00"),
            updatedAt = LocalDateTime.parse("2025-01-01T10:00:00"),
            creator = PreviewUserData.users[0],
            tasks = emptyList()
        ),
        Tag(
            createdBy = PreviewUserData.users[1].uuid,
            taskIds = emptyList(),
            uuid = Uuid.random(),
            name = "Health",
            color = 0xFF00FF00.toInt(),
            label = habitLabelsList[0],
            createdAt = LocalDateTime.parse("2025-01-02T10:00:00"),
            updatedAt = LocalDateTime.parse("2025-01-02T10:00:00"),
            creator = PreviewUserData.users[1],
            tasks = emptyList()
        )
    )

    // Base tasks (empty completion logs)
    private val baseTasks = listOf(
        Task(
            uuid = Uuid.random(),
            title = "Build Android UI",
            description = "Implement task list screen",
            repetition = 0,
            type = TaskType.PROJECT,
            status = TaskStatus.DONE,
            priority = TaskPriority.HIGH,
            creator = PreviewUserData.users[0],
            tags = listOf(tagsList[0]),
            habitLabels = emptyList(),
            taskCompletionLog = emptyList(),
            startDate = LocalDate.parse("2025-01-01"),
            dueDate = LocalDate.parse("2025-01-01")
        ),
        Task(
            uuid = Uuid.random(),
            title = "Morning Run",
            description = "Run 3km every morning",
            repetition = 1,
            type = TaskType.HABIT,
            status = TaskStatus.CANCELLED,
            priority = TaskPriority.MEDIUM,
            creator = PreviewUserData.users[1],
            tags = listOf(tagsList[1]),
            habitLabels = listOf(habitLabelsList[0]),
            taskCompletionLog = emptyList(),
            startDate = LocalDate.parse("2025-01-01"),
            dueDate = LocalDate.parse("2025-01-01")
        ),
        Task(
            uuid = Uuid.random(),
            title = "Read 30 minutes",
            description = "Daily reading habit",
            repetition = 1,
            type = TaskType.HABIT,
            status = TaskStatus.TODO,
            priority = TaskPriority.LOW,
            creator = PreviewUserData.users[2],
            tags = emptyList(),
            habitLabels = listOf(habitLabelsList[1]),
            taskCompletionLog = emptyList(),
            startDate = LocalDate.parse("2025-01-01"),
            dueDate = LocalDate.parse("2025-01-01")
        )
    )

    // Completion logs
    val completionLogs = listOf(
        TaskCompletionLog(
            taskCompletionId = Uuid.random(),
            taskId = baseTasks[0].uuid,
            date = LocalDate.parse("2025-01-01"),
            completedAt = LocalDateTime.parse("2025-01-01T11:00:00"),
            task = baseTasks[0],
            user = PreviewUserData.users[0]
        ),
        TaskCompletionLog(
            taskCompletionId = Uuid.random(),
            taskId = baseTasks[1].uuid,
            date = LocalDate.parse("2025-01-01"),
            completedAt = LocalDateTime.parse("2025-01-01T06:30:00"),
            task = baseTasks[1],
            user = PreviewUserData.users[1]
        )
    )

    // Tasks with logs attached
    val tasks = listOf(
        baseTasks[0].copy(taskCompletionLog = listOf(completionLogs[0])),
        baseTasks[1].copy(taskCompletionLog = listOf(completionLogs[1])),
        baseTasks[2] // no completion yet
    )
}