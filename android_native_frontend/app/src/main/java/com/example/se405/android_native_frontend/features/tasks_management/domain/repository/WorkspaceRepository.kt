@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.domain.repository

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Project
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.WorkspaceMember
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface WorkspaceRepository {
    suspend fun getProjectsByWorkspace(
        userId: Uuid,
        workspaceId: Uuid,
    ): List<Project>

    suspend fun getMembersByWorkspace(
        userId: Uuid,
        workspaceId: Uuid,
    ): List<WorkspaceMember>
}