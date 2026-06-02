@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.domain.entity

import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class Workspace @OptIn(ExperimentalUuidApi::class)
constructor(
    val id: Uuid,
    val name: String,
    val projects: List<Project>,
    val members: List<WorkspaceMember> = emptyList(),
)