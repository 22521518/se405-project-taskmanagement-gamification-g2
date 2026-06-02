@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.repository

import com.example.se405.android.features.tasks_management.domain.entity.Task
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface PersonalTaskRepository {
    suspend fun getPersonalTasks(userId: Uuid): List<Task>
    suspend fun getPersonalTasksByDateRange(userId: Uuid, from: LocalDate, to: LocalDate): List<Task>
}
