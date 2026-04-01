@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.TagRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GetTagsByUser(
    private val repo: TagRepository,
) {
    suspend operator fun invoke(userId: Uuid): List<Tag> {
        return repo.getTagsByUser(userId)
    }
}