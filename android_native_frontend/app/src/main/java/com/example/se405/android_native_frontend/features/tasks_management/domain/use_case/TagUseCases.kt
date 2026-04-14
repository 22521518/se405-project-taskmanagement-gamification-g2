package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case

import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.CreateTag
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.DeleteTag
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetTagsByUser
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetTagsByWorkspace
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetTagsForTaskOwnership
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.UpdateTag

data class TagUseCases(
    val getTagsForTaskOwnership: GetTagsForTaskOwnership,
    val getTagsByWorkspace: GetTagsByWorkspace,
    val getTagsByUser: GetTagsByUser,
    val createTag: CreateTag,
    val updateTag: UpdateTag,
    val deleteTag: DeleteTag,
)