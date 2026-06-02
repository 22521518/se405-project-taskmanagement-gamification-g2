@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.repository

import com.example.se405.android.features.tasks_management.domain.entity.Tag
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TagRepository {
    suspend fun getTagsForTaskOwnership(taskId: Uuid): List<Tag>

    suspend fun getTagsByWorkspace(workspaceId: Uuid, ): List<Tag>

    suspend fun getTagsByUser(userId: Uuid): List<Tag>

    suspend fun createTag(tag: Tag): Optional<Tag>

    suspend fun updateTag(tag: Tag): Optional<Tag>

    suspend fun deleteTag(
        tagId: Uuid,
    ): Optional<Uuid>
}