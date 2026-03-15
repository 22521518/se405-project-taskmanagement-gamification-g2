package com.example.se405.android_native_frontend.features.tasks_management.data.repositoryImpl

import com.example.se405.android_native_frontend.features.tasks_management.data.mapper.TaskMapper
import com.example.se405.android_native_frontend.features.tasks_management.data.remote.TaskApi
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.TaskRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TaskRepositoryImpl(
    private val api: TaskApi,
): TaskRepository {
    @ExperimentalUuidApi
    override suspend fun getTask(userId: Uuid): List<Task> {
        val response = api.getTasks(userId)
        return response.map { TaskMapper.toDomain(it) }
    }
}