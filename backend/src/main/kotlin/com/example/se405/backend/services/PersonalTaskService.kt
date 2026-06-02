package com.example.se405.backend.services

import com.example.se405.backend.database.model.TaskCompletionLogEntity
import com.example.se405.backend.database.model.TaskEntity
import com.example.se405.backend.database.repository.TaskCompletionLogRepository
import com.example.se405.backend.database.repository.TaskRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class PersonalTaskService(
    private val taskRepository: TaskRepository,
    private val taskCompletionLogRepository: TaskCompletionLogRepository,
) {
    fun getPersonalTasks(userId: UUID, targetDate: LocalDate = LocalDate.now()): List<TaskEntity> {
        val tasks = taskRepository.findPersonalTasksByUserIdWithDetails(userId)
        return buildPersonalTaskView(tasks, targetDate, null)
    }

    fun getPersonalTasksByDateRange(
        userId: UUID,
        from: LocalDate,
        to: LocalDate,
    ): List<TaskEntity> {
        val tasks = taskRepository.findPersonalTasksByUserIdWithDetails(userId)
        val rangeStart = if (from.isAfter(to)) to else from
        val rangeEnd = if (from.isAfter(to)) from else to
        val filteredTasks = tasks.filter { task -> taskMatchesRange(task, rangeStart, rangeEnd) }
        return buildPersonalTaskView(filteredTasks, rangeEnd, rangeStart to rangeEnd)
    }

    private fun buildPersonalTaskView(
        tasks: List<TaskEntity>,
        targetDate: LocalDate,
        dateRange: Pair<LocalDate, LocalDate>?,
    ): List<TaskEntity> {
        if (tasks.isEmpty()) return emptyList()
        val taskIds = tasks.mapNotNull { it.uuid }
        val logs = if (taskIds.isEmpty()) emptyList()
        else taskCompletionLogRepository.findByTaskIdInOrderByCompletedAtDesc(taskIds)
        val logsByTaskId = logs.groupBy { it.taskId }

        return tasks.map { task ->
            val taskId = task.uuid ?: return@map task
            val allLogs = logsByTaskId[taskId].orEmpty()
            val filteredLogs = dateRange?.let { range ->
                allLogs.filter { log -> !log.date.isBefore(range.first) && !log.date.isAfter(range.second) }
            } ?: allLogs
            val computedStatus = computeStatus(task, filteredLogs, targetDate)
            task.copy(status = computedStatus)
        }
    }

    private fun computeStatus(
        task: TaskEntity,
        logs: List<TaskCompletionLogEntity>,
        targetDate: LocalDate,
    ): String {
        return if (task.type.equals("HABIT", ignoreCase = true)) {
            val requiredCount = (task.repetition ?: 0).let { if (it <= 0) 1 else it }
            val logsForDate = logs.filter { it.date == targetDate }
            val completedCount = logsForDate.count { it.status.equals("DONE", ignoreCase = true) }
            val isDone = completedCount >= requiredCount
            val isFailed = logsForDate.any { it.status.equals("FAILED", ignoreCase = true) }
            when {
                isDone -> "DONE"
                isFailed -> "FAILED"
                else -> "TODO"
            }
        } else {
            val start = task.startDate ?: task.dueDate ?: return normalizeStatus(task.status) ?: "TODO"
            val end = task.dueDate ?: task.startDate ?: return normalizeStatus(task.status) ?: "TODO"
            val lastLogStatus = logs.maxByOrNull { it.completedAt }?.status
            val currentStatus = normalizeStatus(lastLogStatus) ?: normalizeStatus(task.status)
            when {
                targetDate.isBefore(start) -> currentStatus ?: "TODO"
                targetDate.isAfter(start) && targetDate.isBefore(end) -> currentStatus ?: "IN_PROGRESS"
                else -> currentStatus ?: "FAILED"
            }
        }
    }

    private fun normalizeStatus(status: String?): String? {
        return when (status?.uppercase()) {
            "DONE" -> "DONE"
            "FAILED", "CANCELLED" -> "FAILED"
            "IN_PROGRESS" -> "IN_PROGRESS"
            "TODO" -> "TODO"
            else -> null
        }
    }

    private fun taskMatchesRange(task: TaskEntity, from: LocalDate, to: LocalDate): Boolean {
        if (task.type.equals("HABIT", ignoreCase = true)) return true
        val start = task.startDate ?: task.dueDate ?: return false
        val end = task.dueDate ?: task.startDate ?: return false
        return !end.isBefore(from) && !start.isAfter(to)
    }
}
