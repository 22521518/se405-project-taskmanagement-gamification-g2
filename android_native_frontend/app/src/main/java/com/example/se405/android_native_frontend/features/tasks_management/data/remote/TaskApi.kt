@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.data.remote

import com.example.se405.android_native_frontend.features.tasks_management.data.dto.GetTaskResponse
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TaskApi {
//    @GET("/task")
    suspend fun getTasks(userId: Uuid): List<GetTaskResponse>
    suspend fun createTask(task: Task): Task
    suspend fun updateTask(task: Task): Task
    suspend fun deleteTask(task: Task): Task
}