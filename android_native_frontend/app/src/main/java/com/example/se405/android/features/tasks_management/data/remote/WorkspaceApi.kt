@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.remote

import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface WorkspaceApi {
    suspend fun getProjectsByWorkspace(workspaceId: Uuid): Optional<List<Project>>
    suspend fun getMembersByWorkspace(workspaceId: Uuid): Optional<List<WorkspaceMember>>
}
