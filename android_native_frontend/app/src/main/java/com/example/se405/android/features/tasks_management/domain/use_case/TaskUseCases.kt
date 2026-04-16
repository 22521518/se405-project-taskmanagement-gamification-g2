@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.use_case

import com.example.se405.android.features.tasks_management.domain.entity.Task
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias GetTaskUseCase = suspend (userId: Uuid) -> List<Task>
typealias UpdateTaskUseCase = suspend (task: Task) -> Optional<Task>
typealias DeleteTaskUseCase = suspend (task: Task) -> Optional<Uuid>
typealias CreateTaskUseCase = suspend (task: Task) -> Optional<Task>

data class TaskUseCases(
    val getTask: GetTaskUseCase,
    val updateTask: UpdateTaskUseCase,
    val deleteTask: DeleteTaskUseCase,
    val createTask: CreateTaskUseCase,
)
