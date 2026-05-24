@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.repository

import com.example.se405.android.features.tasks_management.data.remote.TaskApi
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.repository.TaskCompletionLogRepository
import java.time.LocalDate
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TaskCompletionLogRepositoryImpl(
    private val api: TaskApi
) : TaskCompletionLogRepository {
//    override suspend fun getCompletionLogs(taskId: Uuid): List<TaskCompletionLog> {
//        // Since getTasks query already fetches all completion logs mapped to domain,
//        // we usually don't need a separate fetch here, but we return emptyList as fallback.
//        return emptyList()
//    }

    override suspend fun createCompletionLog(
        task: Task,
        userId: Uuid,
        status: TaskStatus,
        date: LocalDate
    ): Optional<TaskCompletionLog> {
        return if (status == TaskStatus.DONE) {
            api.markTaskDone(task.uuid, userId, date)
        } else {
            api.markTaskWontDo(task.uuid, userId, date)
        }
    }
}
