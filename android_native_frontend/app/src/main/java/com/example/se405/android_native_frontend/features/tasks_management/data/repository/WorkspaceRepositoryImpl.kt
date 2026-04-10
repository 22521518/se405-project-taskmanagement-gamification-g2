@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.data.repository

import com.example.se405.android_native_frontend.features.tasks_management.data.remote.WorkspaceApi
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Project
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.WorkspaceRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class WorkspaceRepositoryImpl(
    private val api: WorkspaceApi
) : WorkspaceRepository {
    override suspend fun getProjectsByWorkspace(userId: Uuid, workspaceId: Uuid): List<Project> {
        return api.getProjectsByWorkspace(userId, workspaceId)
    }

    override suspend fun getMembersByWorkspace(userId: Uuid, workspaceId: Uuid): List<WorkspaceMember> {
        return api.getMembersByWorkspace(userId, workspaceId)
    }
}
