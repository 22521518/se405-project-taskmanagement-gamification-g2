package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case

data class TagUseCases(
    val getTagsForTaskOwnership: GetTagsForTaskOwnership,
    val getTagsByWorkspace: GetTagsByWorkspace,
    val getTagsByUser: GetTagsByUser,
    val createTag: CreateTag,
    val updateTag: UpdateTag,
    val deleteTag: DeleteTag,
)