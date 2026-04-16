@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional as GqlOptional
import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.graphql.CreateTagMutation
import com.example.se405.android.graphql.DeleteTagMutation
import com.example.se405.android.graphql.GetTagsByUserQuery
import com.example.se405.android.graphql.GetTagsByWorkspaceQuery
import com.example.se405.android.graphql.GetTagsForTaskOwnershipQuery
import com.example.se405.android.graphql.UpdateTagMutation
import com.example.se405.android.graphql.type.CreateTagInput
import com.example.se405.android.graphql.type.TagOwnershipType as GqlTagOwnershipType
import com.example.se405.android.graphql.type.TaskPriority as GqlTaskPriority
import com.example.se405.android.graphql.type.TaskStatus as GqlTaskStatus
import com.example.se405.android.graphql.type.UpdateTagInput
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TagApiImpl(private val apolloClient: ApolloClient) : TagApi {
    override suspend fun getTagsByWorkspace(workspaceId: Uuid): Optional<List<Tag>> {
        val response = apolloClient.query(GetTagsByWorkspaceQuery(workspaceId.toString())).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val tags = response.data?.getTagsByWorkspace ?: return Optional.empty()
        return Optional.of(tags.map { graphTag ->
            graphTag.toDomainTag(
                fallbackWorkspaceId = workspaceId,
                fallbackCreatedBy = ZERO_UUID,
            )
        })
    }

    override suspend fun getTagsByUser(userId: Uuid): Optional<List<Tag>> {
        val response = apolloClient.query(GetTagsByUserQuery(userId.toString())).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val tags = response.data?.getTagsByUser ?: return Optional.empty()
        return Optional.of(tags.map { graphTag ->
            graphTag.toDomainTag(
                fallbackWorkspaceId = null,
                fallbackCreatedBy = userId,
            )
        })
    }

    override suspend fun getTagsForTaskOwnership(taskId: Uuid): Optional<List<Tag>> {
        val response = apolloClient.query(GetTagsForTaskOwnershipQuery(taskId.toString())).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val tags = response.data?.getTagsForTaskOwnership ?: return Optional.empty()
        return Optional.of(tags.map { graphTag ->
            graphTag.toDomainTag(
                fallbackWorkspaceId = null,
                fallbackCreatedBy = ZERO_UUID,
            )
        })
    }

    override suspend fun createTag(tag: Tag): Optional<Tag> {
        val input = CreateTagInput(
            name = tag.name,
            color = tag.color,
            ownershipType = tag.ownershipType.toGqlTagOwnershipType(),
            createdBy = tag.createdBy.toString(),
            workspaceId = GqlOptional.present(tag.workspaceId?.toString()),
            labelId = GqlOptional.present(tag.label.id.toString()),
        )
        val response = apolloClient.mutation(CreateTagMutation(input)).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val created = response.data?.createTag ?: return Optional.empty()
        return Optional.of(
            created.toDomainTag(
                fallbackWorkspaceId = tag.workspaceId,
                fallbackCreatedBy = tag.createdBy,
            )
        )
    }

    override suspend fun updateTag(tag: Tag): Optional<Tag> {
        val input = UpdateTagInput(
            uuid = tag.uuid.toString(),
            name = GqlOptional.present(tag.name),
            color = GqlOptional.present(tag.color),
            ownershipType = GqlOptional.present(tag.ownershipType.toGqlTagOwnershipType()),
            workspaceId = GqlOptional.present(tag.workspaceId?.toString()),
            labelId = GqlOptional.present(tag.label.id.toString()),
        )
        val response = apolloClient.mutation(UpdateTagMutation(input)).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val updated = response.data?.updateTag ?: return Optional.empty()
        return Optional.of(
            updated.toDomainTag(
                fallbackWorkspaceId = tag.workspaceId,
                fallbackCreatedBy = tag.createdBy,
            )
        )
    }

    override suspend fun deleteTag(tagId: Uuid): Optional<Uuid> {
        val response = apolloClient.mutation(DeleteTagMutation(tagId.toString())).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val deletedUuid = response.data?.deleteTag?.uuid ?: return Optional.empty()
        return Optional.of(Uuid.parse(deletedUuid))
    }

    private fun GetTagsByWorkspaceQuery.GetTagsByWorkspace.toDomainTag(
        fallbackWorkspaceId: Uuid?,
        fallbackCreatedBy: Uuid,
    ): Tag {
        val mappedTasks = tasks.map { it.toDomainTask() }
        return toDomainTag(
        uuid = uuid,
        name = name,
        color = color,
        ownershipType = ownershipType,
        workspaceId = workspaceId,
        createdBy = null,
        createdAt = null,
        updatedAt = null,
        taskIds = mappedTasks.map { it.uuid },
        tasks = mappedTasks,
        fallbackWorkspaceId = fallbackWorkspaceId,
        fallbackCreatedBy = fallbackCreatedBy,
    )
    }

    private fun GetTagsByUserQuery.GetTagsByUser.toDomainTag(
        fallbackWorkspaceId: Uuid?,
        fallbackCreatedBy: Uuid,
    ): Tag {
        val mappedTasks = tasks.map { it.toDomainTask() }
        return toDomainTag(
        uuid = uuid,
        name = name,
        color = color,
        ownershipType = ownershipType,
        workspaceId = workspaceId,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        taskIds = mappedTasks.map { it.uuid },
        tasks = mappedTasks,
        fallbackWorkspaceId = fallbackWorkspaceId,
        fallbackCreatedBy = fallbackCreatedBy,
    )
    }

    private fun GetTagsForTaskOwnershipQuery.GetTagsForTaskOwnership.toDomainTag(
        fallbackWorkspaceId: Uuid?,
        fallbackCreatedBy: Uuid,
    ): Tag {
        val mappedTasks = tasks.map { it.toDomainTask() }
        return toDomainTag(
        uuid = uuid,
        name = name,
        color = color,
        ownershipType = ownershipType,
        workspaceId = workspaceId,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        taskIds = mappedTasks.map { it.uuid },
        tasks = mappedTasks,
        fallbackWorkspaceId = fallbackWorkspaceId,
        fallbackCreatedBy = fallbackCreatedBy,
    )
    }

    private fun CreateTagMutation.CreateTag.toDomainTag(
        fallbackWorkspaceId: Uuid?,
        fallbackCreatedBy: Uuid,
    ): Tag = toDomainTag(
        uuid = uuid,
        name = name,
        color = color,
        ownershipType = ownershipType,
        workspaceId = null,
        createdBy = null,
        createdAt = null,
        updatedAt = null,
        fallbackWorkspaceId = fallbackWorkspaceId,
        fallbackCreatedBy = fallbackCreatedBy,
    )

    private fun UpdateTagMutation.UpdateTag.toDomainTag(
        fallbackWorkspaceId: Uuid?,
        fallbackCreatedBy: Uuid,
    ): Tag = toDomainTag(
        uuid = uuid,
        name = name,
        color = color,
        ownershipType = ownershipType,
        workspaceId = null,
        createdBy = null,
        createdAt = null,
        updatedAt = null,
        fallbackWorkspaceId = fallbackWorkspaceId,
        fallbackCreatedBy = fallbackCreatedBy,
    )

    private fun toDomainTag(
        uuid: String,
        name: String,
        color: Int,
        ownershipType: GqlTagOwnershipType,
        workspaceId: String?,
        createdBy: String?,
        createdAt: String?,
        updatedAt: String?,
        taskIds: List<Uuid> = emptyList(),
        tasks: List<Task> = emptyList(),
        fallbackWorkspaceId: Uuid?,
        fallbackCreatedBy: Uuid,
    ): Tag {
        val resolvedOwnership = ownershipType.toDomainTagOwnershipType()
        val resolvedWorkspaceId = workspaceId.safeUuidOrNull() ?: fallbackWorkspaceId
        val normalizedWorkspaceId =
            if (resolvedOwnership == TagOwnershipType.WORKSPACE) resolvedWorkspaceId else null

        return Tag(
            createdBy = createdBy.safeUuidOrNull() ?: fallbackCreatedBy,
            ownershipType = resolvedOwnership,
            workspaceId = normalizedWorkspaceId,
            taskIds = taskIds,
            uuid = Uuid.parse(uuid),
            name = name,
            color = color,
            label = BuiltinLabels.first(),
            createdAt = createdAt.toLocalDateTimeOrNow(),
            updatedAt = updatedAt.toLocalDateTimeOrNow(),
            creator = null,
            tasks = tasks,
        )
    }

    private fun GetTagsByWorkspaceQuery.Task.toDomainTask(): Task = toDomainTask(
        uuid = uuid,
        title = title,
        status = status,
        priority = priority,
        projectId = projectId,
        startDate = startDate,
        dueDate = dueDate,
    )

    private fun GetTagsByUserQuery.Task.toDomainTask(): Task = toDomainTask(
        uuid = uuid,
        title = title,
        status = status,
        priority = priority,
        projectId = projectId,
        startDate = startDate,
        dueDate = dueDate,
    )

    private fun GetTagsForTaskOwnershipQuery.Task.toDomainTask(): Task = toDomainTask(
        uuid = uuid,
        title = title,
        status = status,
        priority = priority,
        projectId = projectId,
        startDate = startDate,
        dueDate = dueDate,
    )

    private fun toDomainTask(
        uuid: String,
        title: String,
        status: GqlTaskStatus,
        priority: GqlTaskPriority,
        projectId: String?,
        startDate: String?,
        dueDate: String?,
    ): Task {
        val domainProjectId = projectId.safeUuidOrNull()
        return Task(
            uuid = Uuid.parse(uuid),
            title = title,
            description = "",
            repetition = 0,
            type = if (domainProjectId != null) TaskType.PROJECT else TaskType.HABIT,
            status = status.toDomainTaskStatus(),
            priority = priority.toDomainTaskPriority(),
            creator = null,
            tags = emptyList(),
            taskCompletionLog = emptyList(),
            startDate = startDate.toLocalDateOrNull(),
            dueDate = dueDate.toLocalDateOrNull(),
            projectId = domainProjectId,
        )
    }

    private fun GqlTaskStatus.toDomainTaskStatus(): TaskStatus =
        when (this) {
            GqlTaskStatus.CANCELLED -> TaskStatus.FAILED
            GqlTaskStatus.DONE -> TaskStatus.DONE
            GqlTaskStatus.IN_PROGRESS -> TaskStatus.IN_PROGRESS
            GqlTaskStatus.TODO -> TaskStatus.TODO
            else -> TaskStatus.TODO
        }

    private fun GqlTaskPriority.toDomainTaskPriority(): TaskPriority =
        when (this) {
            GqlTaskPriority.LOW -> TaskPriority.LOW
            GqlTaskPriority.MEDIUM -> TaskPriority.MEDIUM
            GqlTaskPriority.HIGH -> TaskPriority.HIGH
            else -> TaskPriority.MEDIUM
        }

    private fun GqlTagOwnershipType.toDomainTagOwnershipType(): TagOwnershipType =
        when (this) {
            GqlTagOwnershipType.WORKSPACE -> TagOwnershipType.WORKSPACE
            GqlTagOwnershipType.PERSONAL -> TagOwnershipType.PERSONAL
            else -> TagOwnershipType.PERSONAL
        }

    private fun TagOwnershipType.toGqlTagOwnershipType(): GqlTagOwnershipType =
        when (this) {
            TagOwnershipType.WORKSPACE -> GqlTagOwnershipType.WORKSPACE
            TagOwnershipType.PERSONAL -> GqlTagOwnershipType.PERSONAL
        }

    private fun String?.toLocalDateTimeOrNow(): LocalDateTime {
        if (this.isNullOrBlank()) return LocalDateTime.now()
        return runCatching { LocalDateTime.parse(this) }.getOrDefault(LocalDateTime.now())
    }

    private fun String?.toLocalDateOrNull(): LocalDate? {
        if (this.isNullOrBlank()) return null
        return runCatching { LocalDate.parse(this) }.getOrNull()
    }

    private fun String?.safeUuidOrNull(): Uuid? {
        if (this.isNullOrBlank()) return null
        return runCatching { Uuid.parse(this) }.getOrNull()
    }

    private companion object {
        val ZERO_UUID: Uuid = Uuid.fromLongs(0L, 0L)
    }
}
