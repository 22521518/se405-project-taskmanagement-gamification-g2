package com.example.se405.android_native_frontend.features.tasks_management.data.mapper

import com.example.se405.android_native_frontend.features.tasks_management.data.dto.GetTaskResponse
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import kotlin.uuid.ExperimentalUuidApi

object TaskMapper {
    @OptIn(ExperimentalUuidApi::class)
    fun toDomain(dto: GetTaskResponse): Task {
        return Task(
            uuid = dto.uuid,
            title = dto.title,
            description = dto.description,
            repetition = dto.repetition,
            type = dto.type,
            status = dto.status,
            priority = dto.priority,
            creator = dto.creator,
            tags = dto.tags,
            habitLabels = dto.habitLabels,
            taskCompletionLog = dto.taskCompletionLog,
            startDate = dto.startDate,
            dueDate = dto.dueDate
            )
    }
}