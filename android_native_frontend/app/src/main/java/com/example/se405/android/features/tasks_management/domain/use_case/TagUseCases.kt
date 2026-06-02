@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.use_case

import com.example.se405.android.features.tasks_management.domain.entity.Tag
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias GetTagsForTaskOwnershipUseCase = suspend (
    taskId: Uuid,
) -> List<Tag>
typealias GetTagsByWorkspaceUseCase = suspend (workspaceId: Uuid) -> List<Tag>
typealias GetTagsByUserUseCase = suspend (userId: Uuid) -> List<Tag>
typealias CreateTagUseCase = suspend (tag: Tag) -> Optional<Tag>
typealias UpdateTagUseCase = suspend (tag: Tag) -> Optional<Tag>
typealias DeleteTagUseCase = suspend (
    tagId: Uuid,
) -> Optional<Uuid>

data class TagUseCases(
    val getTagsForTaskOwnership: GetTagsForTaskOwnershipUseCase,
    val getTagsByWorkspace: GetTagsByWorkspaceUseCase,
    val getTagsByUser: GetTagsByUserUseCase,
    val createTag: CreateTagUseCase,
    val updateTag: UpdateTagUseCase,
    val deleteTag: DeleteTagUseCase,
)