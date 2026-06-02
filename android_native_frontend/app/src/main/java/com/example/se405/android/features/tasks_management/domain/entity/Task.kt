@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.entity

import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Task(
    val uuid: Uuid,
    val title: String,
    val description: String = "",
    val repetition: Int = 0,
    val type: TaskType = TaskType.HABIT,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.LOW,
    val creator: User? = null,
    val tagIds: List<Uuid> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val taskCompletionLog: List<TaskCompletionLog> = emptyList(),
    val assignees: List<User> = emptyList(),

    val startDate: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val projectId: Uuid? = null,
) {
    val assignee: User? get() = assignees.firstOrNull()
    fun getTaskStatus(targetDate: LocalDate): Task = this.copy(status = getTaskStatus(this, targetDate).status)
}



fun getTaskStatus(task: Task, targetDate: LocalDate): Task {
    val taskLogs = task.taskCompletionLog
    if (task.type == TaskType.HABIT) {
        val logs = taskLogs.filter { it.date == targetDate }
        val completedCount = logs.count { it.status == TaskStatus.DONE }
        val requiredCount = task.repetition.let { if (it <= 0) 1 else task.repetition }
        val isDone = completedCount >= (requiredCount)
        val isFailed = logs.any { it.status == TaskStatus.FAILED }
        val statusForDate = when {
            isDone -> TaskStatus.DONE
            isFailed -> TaskStatus.FAILED
            else -> TaskStatus.TODO
        }
        println("DCCMMMMMM [[[[[[$statusForDate]]]]]]")
        return task.copy(status = statusForDate)
    } else {
        val start = task.startDate ?: task.dueDate ?: return task
        val end = task.dueDate ?: task.startDate ?: return task
        val currentStatus = if (taskLogs.isNotEmpty()) taskLogs[0].status else null
        if (targetDate.isBefore(start)) return task.copy(status = currentStatus ?: TaskStatus.TODO)
        if (targetDate.isAfter(start) && targetDate.isBefore(end))
            return task.copy(status = currentStatus ?: TaskStatus.IN_PROGRESS)
        return task.copy(status = currentStatus ?: TaskStatus.FAILED)
    }
}

enum class TaskType { HABIT, PROJECT }
enum class TaskStatus {  TODO, IN_PROGRESS, DONE, FAILED }
enum class TaskPriority { LOW, MEDIUM, HIGH }