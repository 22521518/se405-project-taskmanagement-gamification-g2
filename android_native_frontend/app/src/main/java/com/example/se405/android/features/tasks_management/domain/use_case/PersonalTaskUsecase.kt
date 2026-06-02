@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.use_case

import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.repository.PersonalTaskRepository
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PersonalTaskUsecase(
    private val repository: PersonalTaskRepository,
) {
    suspend fun getAllTasks(userId: Uuid): List<Task> {
        return repository.getPersonalTasks(userId)
    }

    suspend fun getTasksByDateRange(userId: Uuid, from: LocalDate, to: LocalDate): List<Task> {
        return repository.getPersonalTasksByDateRange(userId, from, to)
    }
}
