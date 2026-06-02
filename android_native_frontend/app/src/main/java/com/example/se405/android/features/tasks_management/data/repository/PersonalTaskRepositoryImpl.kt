@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.repository

import com.example.se405.android.features.tasks_management.data.remote.PersonalTaskApi
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.repository.PersonalTaskRepository
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PersonalTaskRepositoryImpl(
    private val api: PersonalTaskApi
) : PersonalTaskRepository {
    override suspend fun getPersonalTasks(userId: Uuid): List<Task> {
        return api.getPersonalTasks(userId).orElse(emptyList())
    }

    override suspend fun getPersonalTasksByDateRange(
        userId: Uuid,
        from: LocalDate,
        to: LocalDate,
    ): List<Task> {
        return api.getPersonalTasksByDateRange(userId, from, to).orElse(emptyList())
    }
}
