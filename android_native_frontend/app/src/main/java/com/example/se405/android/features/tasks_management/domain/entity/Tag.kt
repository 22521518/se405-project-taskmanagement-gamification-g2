package com.example.se405.android.features.tasks_management.domain.entity

import com.example.se405.android.core.presentation.components.HabitLabel
import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Tag @OptIn(ExperimentalUuidApi::class)
constructor(
    val createdBy: Uuid? = null,
    val ownershipType: TagOwnershipType,
    val workspaceId: Uuid?,
    val taskIds: List<Uuid> = emptyList(),

    val uuid: Uuid,
    val name: String,
    val color: Int,
    val label: HabitLabel,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,

    val creator: User? = null,
    val tasks: List<Task> = emptyList(),
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