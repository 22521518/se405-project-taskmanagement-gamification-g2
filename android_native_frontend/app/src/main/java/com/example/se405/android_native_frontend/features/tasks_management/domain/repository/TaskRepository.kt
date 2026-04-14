@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.domain.repository

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TaskRepository {
    suspend fun getTask(userId: Uuid): List<Task>
    suspend fun createTask(task: Task): Task
    suspend fun updateTask(task: Task): Task
    suspend fun deleteTask(task: Task): Task
}