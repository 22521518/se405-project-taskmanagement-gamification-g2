@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.repository

import com.example.se405.android.features.tasks_management.data.remote.TagApi
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.repository.TagRepository
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TagRepositoryImpl(
    private val api: TagApi
) : TagRepository {
    override suspend fun getTagsForTaskOwnership(taskId: Uuid): List<Tag> {
        return api.getTagsForTaskOwnership(taskId).orElse(emptyList())
    }

    override suspend fun getTagsByWorkspace(workspaceId: Uuid): List<Tag> {
        return api.getTagsByWorkspace(workspaceId).orElse(emptyList())
    }

    override suspend fun getTagsByUser(userId: Uuid): List<Tag> {
        return api.getTagsByUser(userId).orElse(emptyList())
    }

    override suspend fun createTag(tag: Tag): Optional<Tag> {
        return api.createTag(tag)
    }

    override suspend fun updateTag(tag: Tag): Optional<Tag> {
        return api.updateTag(tag)
    }

    override suspend fun deleteTag(tagId: Uuid) : Optional<Uuid> {
        return api.deleteTag(tagId)
    }
}
