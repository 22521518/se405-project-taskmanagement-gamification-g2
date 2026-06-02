@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.data.repository

import com.example.se405.android.features.tasks_management.domain.entity.ProjectMember
import com.example.se405.android.features.tasks_management.domain.repository.WorkspaceRepository
import com.example.se405.android.features.workspaces_management.data.remote.CreateGetWorkspaceByWorkspaceIdApi
import com.example.se405.android.features.workspaces_management.data.remote.WorkspaceActivityPage
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import com.example.se405.android.features.workspaces_management.domain.repository.ExtWorkspaceRepositoryWithGet
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExtWorkspaceRepositoryWithGetImpl(
    private val delegate: WorkspaceRepository,
    private val getWorkspaceByWorkspaceIdApi: CreateGetWorkspaceByWorkspaceIdApi,
): ExtWorkspaceRepositoryWithGet {
    override suspend fun getWorkspaceListByUserId(userId: Uuid): List<Workspace> {
        return getWorkspaceByWorkspaceIdApi.getWorkspaceListByUserId(userId)
    }

    override suspend fun getProjectsByWorkspace(workspaceId: Uuid) =
        delegate.getProjectsByWorkspace(workspaceId)

    override suspend fun getMembersByWorkspace(workspaceId: Uuid) =
        delegate.getMembersByWorkspace(workspaceId)

    override suspend fun getWorkspaceByWorkspaceId(workspaceId: Uuid): Optional<Workspace> {
        return getWorkspaceByWorkspaceIdApi.getWorkspaceById(workspaceId)
    }

    override suspend fun getMembersByProjectId(projectId: Uuid): List<ProjectMember> {
        return getWorkspaceByWorkspaceIdApi.getMemberByProject(projectId)
    }

    override suspend fun getWorkspaceActivity(
        workspaceId: Uuid,
        page: Int?,
        size: Int?
    ): WorkspaceActivityPage {
        return getWorkspaceByWorkspaceIdApi.getWorkspaceActivity(workspaceId, page, size)
    }
}