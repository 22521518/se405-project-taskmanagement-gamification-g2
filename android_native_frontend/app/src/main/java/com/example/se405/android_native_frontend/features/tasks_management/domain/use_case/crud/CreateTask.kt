package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.TaskRepository

class CreateTask(
    private val repo: TaskRepository
) {
    suspend operator fun invoke(task: Task): Task {
        return repo.createTask(task)
    }
}