@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.TagRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GetTagsForTaskOwnership(
    private val repo: TagRepository,
) {
    suspend operator fun invoke(
        userId: Uuid,
        isProjectTask: Boolean,
        workspaceId: Uuid? = null,
    ): List<Tag> {
        return if (isProjectTask) {
            requireNotNull(workspaceId) { "workspaceId is required for project tasks" }
            repo.getTagsByWorkspace(userId, workspaceId)
        } else {
            repo.getTagsByUser(userId)
        }
    }
}