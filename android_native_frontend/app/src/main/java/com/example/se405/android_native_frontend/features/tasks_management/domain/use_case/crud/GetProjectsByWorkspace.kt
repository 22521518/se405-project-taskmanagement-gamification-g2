@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Project
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.WorkspaceRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GetProjectsByWorkspace(
    private val repo: WorkspaceRepository,
) {
    suspend operator fun invoke(
        userId: Uuid,
        workspaceId: Uuid,
    ): List<Project> {
        return repo.getProjectsByWorkspace(userId, workspaceId)
    }
}