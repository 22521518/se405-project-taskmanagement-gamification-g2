@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.use_case

import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias GetProjectsByWorkspaceUseCase = suspend (workspaceId: Uuid) -> List<Project>
typealias GetMembersByWorkspaceUseCase = suspend (workspaceId: Uuid) -> List<WorkspaceMember>

data class WorkspaceUseCases(
    val getProjectsByWorkspace: GetProjectsByWorkspaceUseCase,
    val getMembersByWorkspace: GetMembersByWorkspaceUseCase,
)