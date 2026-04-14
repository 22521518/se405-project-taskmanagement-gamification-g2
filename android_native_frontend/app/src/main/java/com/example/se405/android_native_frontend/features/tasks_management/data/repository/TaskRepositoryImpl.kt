@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.data.repository

import com.example.se405.android_native_frontend.features.tasks_management.data.remote.TaskApi
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.TaskRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TaskRepositoryImpl(
    private val api: TaskApi
) : TaskRepository {
    override suspend fun getTask(userId: Uuid): List<Task> {
        val responses = api.getTasks(userId)
        return responses.map { response ->
            Task(
                uuid = response.uuid,
                title = response.title,
                description = response.description,
                repetition = response.repetition,
                type = response.type,
                status = response.status,
                priority = response.priority,
                creator = response.creator,
                tags = response.tags,
                taskCompletionLog = response.taskCompletionLog,
                startDate = response.startDate,
                dueDate = response.dueDate
            )
        }
    }

    override suspend fun createTask(task: Task): Task {
        val response = api.createTask(task)
        return Task(
                uuid = response.uuid,
                title = response.title,
                description = response.description,
                repetition = response.repetition,
                type = response.type,
                status = response.status,
                priority = response.priority,
                creator = response.creator,
                tags = response.tags,
                taskCompletionLog = response.taskCompletionLog,
                startDate = response.startDate,
                dueDate = response.dueDate
            )
    }

    override suspend fun updateTask(task: Task): Task {
        val response = api.updateTask(task)
        return Task(
                uuid = response.uuid,
                title = response.title,
                description = response.description,
                repetition = response.repetition,
                type = response.type,
                status = response.status,
                priority = response.priority,
                creator = response.creator,
                tags = response.tags,
                taskCompletionLog = response.taskCompletionLog,
                startDate = response.startDate,
                dueDate = response.dueDate
            )
    }

    override suspend fun deleteTask(task: Task): Task {
        val response = api.deleteTask(task)
        return Task(
                uuid = response.uuid,
                title = response.title,
                description = response.description,
                repetition = response.repetition,
                type = response.type,
                status = response.status,
                priority = response.priority,
                creator = response.creator,
                tags = response.tags,
                taskCompletionLog = response.taskCompletionLog,
                startDate = response.startDate,
                dueDate = response.dueDate
            )
    }
}
