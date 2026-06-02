@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.use_case

import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.tasks_management.domain.repository.WorkspaceRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

//typealias GetProjectsByWorkspaceUseCase = suspend (workspaceId: Uuid) -> List<Project>
//typealias GetMembersByWorkspaceUseCase = suspend (workspaceId: Uuid): List<WorkspaceMember>

class WorkspaceUseCases(private val workspaceRepository: WorkspaceRepository) {
    suspend fun getProjectsByWorkspace(workspaceId: Uuid): List<Project> {
        return workspaceRepository.getProjectsByWorkspace(workspaceId)
    }
    suspend fun getMembersByWorkspace(workspaceId: Uuid): List<WorkspaceMember> {
        return workspaceRepository.getMembersByWorkspace(workspaceId)
    }
}