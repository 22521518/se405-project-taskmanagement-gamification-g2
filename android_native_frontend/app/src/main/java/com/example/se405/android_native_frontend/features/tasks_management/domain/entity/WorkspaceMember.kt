package com.example.se405.android_native_frontend.features.tasks_management.domain.entity

import com.example.se405.android_native_frontend.features.users_management.domain.entity.User
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class WorkspaceMember @OptIn(ExperimentalUuidApi::class)
constructor(
    val workspaceId: Uuid,
    val userId: Uuid,
    val role: WorkspaceRole,
    val joinedAt: LocalDateTime,
    val user: User?,
)

enum class WorkspaceRole {
    OWNER,
    MEMBER,
}