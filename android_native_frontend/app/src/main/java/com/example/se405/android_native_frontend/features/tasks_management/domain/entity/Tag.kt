package com.example.se405.android_native_frontend.features.tasks_management.domain.entity

import com.example.se405.android_native_frontend.core.presentation.components.HabitLabel
import com.example.se405.android_native_frontend.features.users_management.domain.entity.User
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Tag @OptIn(ExperimentalUuidApi::class)
constructor(
    val createdBy: Uuid,
    val ownershipType: TagOwnershipType,
    val workspaceId: Uuid?,
    val taskIds: List<Uuid>,

    val uuid: Uuid,
    val name: String,
    val color: Int,
    val label: HabitLabel,

    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,

    val creator: User?,
    val tasks: List<Task>,
    ) {
    init {
        require(name.isNotBlank()) { "Tag name must not be blank" }

        when (ownershipType) {
            TagOwnershipType.WORKSPACE -> {
                require(workspaceId != null) { "Workspace tag must have workspaceId" }
            }
            TagOwnershipType.PERSONAL -> {
                require(workspaceId == null) { "Personal tag must not have workspaceId" }
            }
        }
    }
}

enum class TagOwnershipType {
    WORKSPACE,
    PERSONAL,
}