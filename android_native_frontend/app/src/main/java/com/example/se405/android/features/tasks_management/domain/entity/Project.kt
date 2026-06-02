package com.example.se405.android.features.tasks_management.domain.entity

import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Project @OptIn(ExperimentalUuidApi::class)
constructor(
    val id: Uuid,
    val name: String,
    val workspaceId: Uuid? = null,
    val tasks: List<Task>,
    val member: List<ProjectMember> = emptyList(),
    val createdAt: LocalDateTime? = null,
)