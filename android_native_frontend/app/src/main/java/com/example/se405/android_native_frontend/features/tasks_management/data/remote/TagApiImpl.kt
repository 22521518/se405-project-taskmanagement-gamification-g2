@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.data.remote

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TagApiImpl : TagApi {
    override suspend fun getTagsByWorkspace(userId: Uuid, workspaceId: Uuid): List<Tag> = emptyList()
    override suspend fun getTagsByUser(userId: Uuid): List<Tag> = emptyList()
    override suspend fun createTag(tag: Tag): Tag = tag
    override suspend fun updateTag(tag: Tag): Tag = tag
    override suspend fun deleteTag(tagId: Uuid, userId: Uuid, workspaceId: Uuid?) {}
}
