@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.remote

import com.example.se405.android.features.tasks_management.domain.entity.Task
import java.time.LocalDate
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface PersonalTaskApi {
    suspend fun getPersonalTasks(userId: Uuid): Optional<List<Task>>
    suspend fun getPersonalTasksByDateRange(
        userId: Uuid,
        from: LocalDate,
        to: LocalDate,
    ): Optional<List<Task>>
}
