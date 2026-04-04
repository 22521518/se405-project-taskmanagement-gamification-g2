@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case

import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.TagRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DeleteTag(
    private val repo: TagRepository,
) {
    suspend operator fun invoke(
        tagId: Uuid,
        userId: Uuid,
        workspaceId: Uuid? = null,
    ) {
        repo.deleteTag(tagId, userId, workspaceId)
    }
}