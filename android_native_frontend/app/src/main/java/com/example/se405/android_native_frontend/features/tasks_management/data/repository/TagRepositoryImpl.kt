@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.data.repository

import com.example.se405.android_native_frontend.features.tasks_management.data.remote.TagApi
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.TagRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TagRepositoryImpl(
    private val api: TagApi
) : TagRepository {
    override suspend fun getTagsByWorkspace(userId: Uuid, workspaceId: Uuid): List<Tag> {
        return api.getTagsByWorkspace(userId, workspaceId)
    }

    override suspend fun getTagsByUser(userId: Uuid): List<Tag> {
        return api.getTagsByUser(userId)
    }

    override suspend fun createTag(tag: Tag): Tag {
        return api.createTag(tag)
    }

    override suspend fun updateTag(tag: Tag): Tag {
        return api.updateTag(tag)
    }

    override suspend fun deleteTag(tagId: Uuid, userId: Uuid, workspaceId: Uuid?) {
        api.deleteTag(tagId, userId, workspaceId)
    }
}
