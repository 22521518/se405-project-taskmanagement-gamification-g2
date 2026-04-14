package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case

import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.CreateTask
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetTask
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.UpdateTask

data class TaskUseCases(
    val getTask: GetTask,
    val updateTask: UpdateTask,
    val deleteTaskUseCases: TaskUseCases,
    val createTask: CreateTask,
)
