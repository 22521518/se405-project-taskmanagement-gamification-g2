@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.remote

import com.example.se405.android.features.tasks_management.domain.entity.Task
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TaskApi {
    suspend fun getTasks(userId: Uuid): Optional<List<Task>>
    suspend fun createTask(task: Task): Optional<Task>
    suspend fun updateTask(task: Task): Optional<Task>
    suspend fun deleteTask(task: Task): Optional<Uuid>
}