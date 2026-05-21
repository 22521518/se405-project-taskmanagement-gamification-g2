@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.repository

import com.example.se405.android.features.tasks_management.data.remote.TaskApi
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.repository.TaskRepository
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TaskRepositoryImpl(
    private val api: TaskApi
) : TaskRepository {
    override suspend fun getTask(userId: Uuid): List<Task> {
        return api.getTasks(userId).orElse(emptyList())
    }

    override suspend fun createTask(task: Task, creatorId: Uuid?): Optional<Task> {
        return api.createTask(task, creatorId)
    }

    override suspend fun updateTask(task: Task): Optional<Task> {
        return api.updateTask(task)
    }

    override suspend fun deleteTask(task: Task): Optional<Uuid> {
        return api.deleteTask(task)
    }
}
