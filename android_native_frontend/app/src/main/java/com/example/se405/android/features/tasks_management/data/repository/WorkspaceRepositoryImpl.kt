@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.repository

import com.example.se405.android.features.tasks_management.data.remote.WorkspaceApi
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.tasks_management.domain.repository.WorkspaceRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class WorkspaceRepositoryImpl(
    private val api: WorkspaceApi
) : WorkspaceRepository {
    override suspend fun getProjectsByWorkspace(workspaceId: Uuid): List<Project> {
        return api.getProjectsByWorkspace(workspaceId).orElse(emptyList())
    }

    override suspend fun getMembersByWorkspace(workspaceId: Uuid): List<WorkspaceMember> {
        return api.getMembersByWorkspace(workspaceId).orElse(emptyList())
    }
}
