@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.data.remote

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Project
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.WorkspaceMember
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class WorkspaceApiImpl : WorkspaceApi {
    override suspend fun getProjectsByWorkspace(userId: Uuid, workspaceId: Uuid): List<Project> = emptyList()
    override suspend fun getMembersByWorkspace(userId: Uuid, workspaceId: Uuid): List<WorkspaceMember> = emptyList()
}
