@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.domain.repository

import com.example.se405.android.features.tasks_management.domain.entity.ProjectMember
import com.example.se405.android.features.tasks_management.domain.repository.WorkspaceRepository
import com.example.se405.android.features.workspaces_management.data.remote.WorkspaceActivityPage
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface ExtWorkspaceRepositoryWithGet: WorkspaceRepository {
    suspend fun getWorkspaceListByUserId(userId: Uuid): List<Workspace>
    suspend fun getWorkspaceByWorkspaceId(workspaceId: Uuid): Optional<Workspace>
    suspend fun getMembersByProjectId(projectId: Uuid): List<ProjectMember>
    suspend fun getWorkspaceActivity(workspaceId: Uuid, page: Int? = null, size: Int? = null): WorkspaceActivityPage
}