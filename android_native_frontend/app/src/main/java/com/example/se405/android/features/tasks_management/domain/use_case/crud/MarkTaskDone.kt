@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.use_case.crud

import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.repository.TaskCompletionLogRepository
import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Marks a task as "Done" for a given date.
 *
 * Triggered by the [onDone] callback in TaskDetailPopUp.
 * Creates a TaskCompletionLog entry via the repository.
 *
 * @param task  The task being completed.
 * @param user  The user performing the action.
 * @param date  The calendar date to record completion for (defaults to today).
 */
class MarkTaskDone(
    private val repo: TaskCompletionLogRepository,
) {
    suspend operator fun invoke(
        task: Task,
        user: User,
        date: LocalDate = LocalDate.now(),
    ): Optional<TaskCompletionLog> {
//        val log = TaskCompletionLog(
//            taskCompletionId = Uuid.random(),
//            status = TaskStatus.DONE,
//            date = date,
//            completedAt = LocalDateTime.now(),
//            task = task.uuid,
//            user = user.uuid,
//        )
        return repo.createCompletionLog(task, user, TaskStatus.DONE)
    }
}
