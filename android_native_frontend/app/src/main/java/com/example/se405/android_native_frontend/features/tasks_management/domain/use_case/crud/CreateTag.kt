package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud

import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.TagRepository

class CreateTag(
    private val repo: TagRepository,
) {
    suspend operator fun invoke(tag: Tag): Tag {
        return repo.createTag(tag)
    }
}