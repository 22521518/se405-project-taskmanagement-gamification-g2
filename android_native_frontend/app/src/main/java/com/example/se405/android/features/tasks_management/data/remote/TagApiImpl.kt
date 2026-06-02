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
import com.example.se405.android.features.users_management.domain.entity.User
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
        android.util.Log.d("TagApiImpl", "getTagsByWorkspace: workspaceId=$workspaceId")
        return try {
            val response = apolloClient.query(GetTagsByWorkspaceQuery(workspaceId.toString())).execute()
            if (response.exception != null) {
                android.util.Log.e("TagApiImpl", "getTagsByWorkspace HTTP exception: ", response.exception)
                return Optional.empty()
            }
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TagApiImpl", "getTagsByWorkspace response errors: ${response.errors}")
                Optional.empty()
            } else {
                val tags = response.data?.getTagsByWorkspace ?: run {
                    android.util.Log.w("TagApiImpl", "getTagsByWorkspace response data is empty")
                    return Optional.empty()
                }
                android.util.Log.i("TagApiImpl", "getTagsByWorkspace success: returned ${tags.size} tags")
                Optional.of(tags.map { graphTag ->
                    graphTag.toDomainTag(
                        fallbackWorkspaceId = workspaceId,
                        fallbackCreatedBy = ZERO_UUID,
                    )
                })
            }
        } catch (e: Exception) {
            android.util.Log.e("TagApiImpl", "getTagsByWorkspace exception", e)
            Optional.empty()
        }
    }

    override suspend fun getTagsByUser(userId: Uuid): Optional<List<Tag>> {
        android.util.Log.d("TagApiImpl", "getTagsByUser: userId=$userId")
        return try {
            val response = apolloClient.query(GetTagsByUserQuery(userId.toString())).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TagApiImpl", "getTagsByUser response errors: ${response.errors}")
                Optional.empty()
            } else {
                val tags = response.data?.getTagsByUser ?: run {
                    android.util.Log.w("TagApiImpl", "getTagsByUser response data is empty")
                    return Optional.empty()
                }
                android.util.Log.i("TagApiImpl", "getTagsByUser success: returned ${tags.size} tags")
                Optional.of(tags.map { graphTag ->
                    graphTag.toDomainTag(
                        fallbackWorkspaceId = null,
                        fallbackCreatedBy = userId,
                    )
                })
            }
        } catch (e: Exception) {
            android.util.Log.e("TagApiImpl", "getTagsByUser exception", e)
            Optional.empty()
        }
    }

    override suspend fun getTagsForTaskOwnership(taskId: Uuid): Optional<List<Tag>> {
        android.util.Log.d("TagApiImpl", "getTagsForTaskOwnership: taskId=$taskId")
        return try {
            val response = apolloClient.query(GetTagsForTaskOwnershipQuery(taskId.toString())).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TagApiImpl", "getTagsForTaskOwnership response errors: ${response.errors}")
                Optional.empty()
            } else {
                val tags = response.data?.getTagsForTaskOwnership ?: run {
                    android.util.Log.w("TagApiImpl", "getTagsForTaskOwnership response data is empty")
                    return Optional.empty()
                }
                android.util.Log.i("TagApiImpl", "getTagsForTaskOwnership success: returned ${tags.size} tags")
                Optional.of(tags.map { graphTag ->
                    graphTag.toDomainTag(
                        fallbackWorkspaceId = null,
                        fallbackCreatedBy = ZERO_UUID,
                    )
                })
            }
        } catch (e: Exception) {
            android.util.Log.e("TagApiImpl", "getTagsForTaskOwnership exception", e)
            Optional.empty()
        }
    }

    override suspend fun createTag(tag: Tag): Optional<Tag> {
        android.util.Log.d("TagApiImpl", "createTag: name=${tag.name}, color=${tag.color}")
        return try {
            val input = CreateTagInput(
                name = tag.name,
                color = tag.color,
                ownershipType = tag.ownershipType.toGqlTagOwnershipType(),
                createdBy = tag.createdBy.toString(),
                workspaceId = GqlOptional.present(tag.workspaceId?.toString()),
                labelId = GqlOptional.present(tag.label.id.toString()),
            )
            android.util.Log.d("TagApiImpl", "createTag inputs: $input")
            val response = apolloClient.mutation(CreateTagMutation(input)).execute()
            if (response.exception != null) {
                android.util.Log.e("TagApiImpl", "createTag HTTP exception", response.exception)
                return Optional.empty()
            }
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TagApiImpl", "createTag response errors: ${response.errors}")
                return Optional.empty()
            }

            val created = response.data?.createTag ?: run {
                android.util.Log.e("TagApiImpl", "createTag response data is empty")
                return Optional.empty()
            }
            android.util.Log.i("TagApiImpl", "createTag success: uuid=${created.uuid}")
            Optional.of(
                created.toDomainTag(
                    fallbackWorkspaceId = tag.workspaceId,
                    fallbackCreatedBy = tag.createdBy ?: ZERO_UUID,
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("TagApiImpl", "createTag exception", e)
            Optional.empty()
        }
    }

    override suspend fun updateTag(tag: Tag): Optional<Tag> {
        android.util.Log.d("TagApiImpl", "updateTag: uuid=${tag.uuid}, name=${tag.name}")
        return try {
            val input = UpdateTagInput(
                uuid = tag.uuid.toString(),
                name = GqlOptional.present(tag.name),
                color = GqlOptional.present(tag.color),
                ownershipType = GqlOptional.present(tag.ownershipType.toGqlTagOwnershipType()),
                workspaceId = GqlOptional.present(tag.workspaceId?.toString()),
                labelId = GqlOptional.present(tag.label.id.toString()),
            )
            android.util.Log.d("TagApiImpl", "updateTag inputs: $input")
            val response = apolloClient.mutation(UpdateTagMutation(input)).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TagApiImpl", "updateTag response errors: ${response.errors}")
                return Optional.empty()
            }

            val updated = response.data?.updateTag ?: run {
                android.util.Log.e("TagApiImpl", "updateTag response data is empty")
                return Optional.empty()
            }
            android.util.Log.i("TagApiImpl", "updateTag success: uuid=${updated.uuid}")
            Optional.of(
                updated.toDomainTag(
                    fallbackWorkspaceId = tag.workspaceId,
                    fallbackCreatedBy = tag.createdBy ?: ZERO_UUID,
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("TagApiImpl", "updateTag exception", e)
            Optional.empty()
        }
    }

    override suspend fun deleteTag(tagId: Uuid): Optional<Uuid> {
        android.util.Log.d("TagApiImpl", "deleteTag: tagId=$tagId")
        return try {
            val response = apolloClient.mutation(DeleteTagMutation(tagId.toString())).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TagApiImpl", "deleteTag response errors: ${response.errors}")
                return Optional.empty()
            }

            val deletedUuid = response.data?.deleteTag?.uuid ?: run {
                android.util.Log.e("TagApiImpl", "deleteTag response data is empty")
                return Optional.empty()
            }
            android.util.Log.i("TagApiImpl", "deleteTag success: deletedUuid=$deletedUuid")
            Optional.of(Uuid.parse(deletedUuid))
        } catch (e: Exception) {
            android.util.Log.e("TagApiImpl", "deleteTag exception", e)
            Optional.empty()
        }
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
        creator = creator?.let {
            toDomainUser(it.uuid, it.email, it.username, it.displayName, it.avatarUrl, it.createdAt, it.updatedAt)
        },
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
        creator = creator?.let {
            toDomainUser(it.uuid, it.email, it.username, it.displayName, it.avatarUrl, it.createdAt, it.updatedAt)
        },
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
        creator = creator?.let {
            toDomainUser(it.uuid, it.email, it.username, it.displayName, it.avatarUrl, it.createdAt, it.updatedAt)
        },
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
        creator = creator?.let {
            toDomainUser(it.uuid, it.email, it.username, it.displayName, it.avatarUrl, it.createdAt, it.updatedAt)
        },
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
        creator = creator?.let {
            toDomainUser(it.uuid, it.email, it.username, it.displayName, it.avatarUrl, it.createdAt, it.updatedAt)
        },
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
        creator: User?,
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
            creator = creator,
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
        assignees = assignees.map { assignee ->
            toDomainUser(
                uuid = assignee.user.uuid,
                email = assignee.user.email,
                username = assignee.user.username,
                displayName = assignee.user.displayName,
                avatarUrl = assignee.user.avatarUrl,
                createdAt = "",
                updatedAt = ""
            )
        }
    )

    private fun GetTagsByUserQuery.Task.toDomainTask(): Task = toDomainTask(
        uuid = uuid,
        title = title,
        status = status,
        priority = priority,
        projectId = projectId,
        startDate = startDate,
        dueDate = dueDate,
        assignees = assignees.map { assignee ->
            toDomainUser(
                uuid = assignee.user.uuid,
                email = assignee.user.email,
                username = assignee.user.username,
                displayName = assignee.user.displayName,
                avatarUrl = assignee.user.avatarUrl,
                createdAt = "",
                updatedAt = ""
            )
        }
    )

    private fun GetTagsForTaskOwnershipQuery.Task.toDomainTask(): Task = toDomainTask(
        uuid = uuid,
        title = title,
        status = status,
        priority = priority,
        projectId = projectId,
        startDate = startDate,
        dueDate = dueDate,
        assignees = assignees.map { assignee ->
            toDomainUser(
                uuid = assignee.user.uuid,
                email = assignee.user.email,
                username = assignee.user.username,
                displayName = assignee.user.displayName,
                avatarUrl = assignee.user.avatarUrl,
                createdAt = "",
                updatedAt = ""
            )
        }
    )

    private fun toDomainTask(
        uuid: String,
        title: String,
        status: GqlTaskStatus,
        priority: GqlTaskPriority,
        projectId: String?,
        startDate: String?,
        dueDate: String?,
        assignees: List<User> = emptyList(),
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
            assignees = assignees,
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

    private fun String?.safeUuidOrNull(): Uuid? {
        if (this.isNullOrBlank()) return null
        return runCatching { Uuid.parse(this) }.getOrNull()
    }

    private fun toDomainUser(
        uuid: String,
        email: String,
        username: String,
        displayName: String,
        avatarUrl: String?,
        createdAt: String,
        updatedAt: String
    ): User {
        return User(
            uuid = Uuid.parse(uuid),
            email = email,
            username = username,
            passwordHash = null,
            displayName = displayName,
            avatarUrl = avatarUrl,
            createdAt = createdAt.toLocalDateTimeOrNow(),
            updatedAt = updatedAt.toLocalDateTimeOrNow()
        )
    }

    private companion object {
        val ZERO_UUID: Uuid = Uuid.fromLongs(0L, 0L)
    }
}

fun String?.toLocalDateOrNull(): LocalDate? {
    if (this.isNullOrBlank()) return null
    return runCatching { LocalDate.parse(this) }.getOrNull()
}