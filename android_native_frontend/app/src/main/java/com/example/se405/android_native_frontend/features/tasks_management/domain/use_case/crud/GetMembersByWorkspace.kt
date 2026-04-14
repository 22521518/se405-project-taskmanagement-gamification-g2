@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.WorkspaceRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GetMembersByWorkspace(
    private val repo: WorkspaceRepository,
) {
    suspend operator fun invoke(
        userId: Uuid,
        workspaceId: Uuid,
    ): List<WorkspaceMember> {
        return repo.getMembersByWorkspace(userId, workspaceId)
    }
}