@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.remote

import com.example.se405.android.features.tasks_management.domain.entity.Tag
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TagApi {
    suspend fun getTagsByWorkspace(workspaceId: Uuid): Optional<List<Tag>>
    suspend fun getTagsByUser(userId: Uuid): Optional<List<Tag>>
    suspend fun getTagsForTaskOwnership(taskId: Uuid): Optional<List<Tag>>
    suspend fun createTag(tag: Tag): Optional<Tag>
    suspend fun updateTag(tag: Tag): Optional<Tag>
    suspend fun deleteTag(tagId: Uuid): Optional<Uuid>
}
