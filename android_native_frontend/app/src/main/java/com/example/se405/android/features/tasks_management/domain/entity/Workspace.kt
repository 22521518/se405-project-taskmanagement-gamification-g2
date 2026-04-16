package com.example.se405.android.features.tasks_management.domain.entity

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Workspace @OptIn(ExperimentalUuidApi::class)
constructor(
    val id: Uuid,
    val name: String,
    val projects: List<Project>,
    val members: List<WorkspaceMember> = emptyList(),
)