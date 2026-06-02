@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.repository

import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import java.time.LocalDate
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TaskCompletionLogRepository {
//    suspend fun getCompletionLogs(taskId: Uuid): List<TaskCompletionLog>
    suspend fun createCompletionLog(
        task: Task,
        userId: Uuid,
        status: TaskStatus,
        date: LocalDate = LocalDate.now(),
    ): Optional<TaskCompletionLog>
}
