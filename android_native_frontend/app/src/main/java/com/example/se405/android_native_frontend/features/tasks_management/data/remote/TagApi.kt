@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.data.remote

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TagApi {
    suspend fun getTagsByWorkspace(userId: Uuid, workspaceId: Uuid): List<Tag>
    suspend fun getTagsByUser(userId: Uuid): List<Tag>
    suspend fun createTag(tag: Tag): Tag
    suspend fun updateTag(tag: Tag): Tag
    suspend fun deleteTag(tagId: Uuid, userId: Uuid, workspaceId: Uuid?)
}
