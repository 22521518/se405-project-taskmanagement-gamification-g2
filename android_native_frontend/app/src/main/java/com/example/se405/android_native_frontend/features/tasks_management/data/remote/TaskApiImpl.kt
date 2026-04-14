@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.data.remote

import com.example.se405.android_native_frontend.features.tasks_management.data.dto.GetTaskResponse
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TaskApiImpl : TaskApi {
    override suspend fun getTasks(userId: Uuid): List<GetTaskResponse> = emptyList()

    override suspend fun createTask(task: Task): Task = task

    override suspend fun updateTask(task: Task): Task = task

    override suspend fun deleteTask(task: Task): Task = task
}
