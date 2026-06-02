@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.use_case.crud

import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.repository.TaskCompletionLogRepository
import java.time.LocalDate
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Marks a task as "Won't Do" for a given date.
 *
 * Triggered by the [onWontDo] callback in TaskDetailPopUp.
 * Semantically equivalent to Done in storage — callers distinguish
 * the action type at the UI layer. A future iteration can add a
 * `completionType` field to TaskCompletionLog to differentiate.
 *
 * @param task  The task being skipped.
 * @param user  The user performing the action.
 * @param date  The calendar date to record (defaults to today).
 */
class   MarkTaskWontDo(
    private val repo: TaskCompletionLogRepository,
) {
    suspend operator fun invoke(
        task: Task,
        userId: Uuid,
        date: LocalDate = LocalDate.now(),
    ): Optional<TaskCompletionLog> {
        return repo.createCompletionLog(task, userId, TaskStatus.FAILED)
    }
}
