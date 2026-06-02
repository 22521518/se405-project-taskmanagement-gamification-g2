package com.example.se405.android.features.tasks_management.domain.entity

import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ProjectMember @OptIn(ExperimentalUuidApi::class)
constructor(
    val projectId: Uuid,
    val workspaceId: Uuid,
    val userId: Uuid,

    val joinedAt: LocalDateTime?,
    val user: User?,
)