@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.remote

import com.apollographql.apollo.ApolloClient
import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.entity.getTaskStatus
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.graphql.PersonalTasksByDateRangeQuery
import com.example.se405.android.graphql.PersonalTasksQuery
import com.example.se405.android.graphql.type.TagOwnershipType as GqlTagOwnershipType
import com.example.se405.android.graphql.type.TaskPriority as GqlTaskPriority
import com.example.se405.android.graphql.type.TaskStatus as GqlTaskStatus
import com.example.se405.android.graphql.type.TaskType as GqlTaskType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PersonalTaskApiImpl(private val apolloClient: ApolloClient) : PersonalTaskApi {
    override suspend fun getPersonalTasks(userId: Uuid): Optional<List<Task>> {
        android.util.Log.d("PersonalTaskApiImpl", "getPersonalTasks: userId=$userId")
        return try {
            val response = apolloClient.query(PersonalTasksQuery(userId.toString())).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("PersonalTaskApiImpl", "getPersonalTasks response errors: ${response.errors}")
                return Optional.empty()
            }

            val tasks = response.data?.personalTasks ?: run {
                android.util.Log.w("PersonalTaskApiImpl", "getPersonalTasks response data is empty")
                return Optional.empty()
            }

            val targetDate = LocalDate.now()
            Optional.of(tasks.map { it.toDomainTask(targetDate) })
        } catch (e: Exception) {
            android.util.Log.e("PersonalTaskApiImpl", "getPersonalTasks exception", e)
            Optional.empty()
        }
    }

    override suspend fun getPersonalTasksByDateRange(
        userId: Uuid,
        from: LocalDate,
        to: LocalDate,
    ): Optional<List<Task>> {
        android.util.Log.d("PersonalTaskApiImpl", "getPersonalTasksByDateRange: userId=$userId, from=$from, to=$to")
        return try {
            val response = apolloClient.query(
                PersonalTasksByDateRangeQuery(
                    userId = userId.toString(),
                    from = from.toString(),
                    to = to.toString(),
                )
            ).execute()

            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("PersonalTaskApiImpl", "getPersonalTasksByDateRange response errors: ${response.errors}")
                return Optional.empty()
            }

            val tasks = response.data?.personalTasksByDateRange ?: run {
                android.util.Log.w("PersonalTaskApiImpl", "getPersonalTasksByDateRange response data is empty")
                return Optional.empty()
            }

            Optional.of(tasks.map { it.toDomainTask(to) })
        } catch (e: Exception) {
            android.util.Log.e("PersonalTaskApiImpl", "getPersonalTasksByDateRange exception", e)
            Optional.empty()
        }
    }

    private fun PersonalTasksQuery.PersonalTask.toDomainTask(targetDate: LocalDate): Task {
        val domainTask = Task(
            uuid = Uuid.parse(uuid),
            title = title,
            description = description.orEmpty(),
            repetition = repetition ?: 0,
            type = type.toDomainTaskType(),
            status = status.toDomainTaskStatus(),
            priority = priority.toDomainTaskPriority(),
            creator = creator?.let {
                toDomainUser(
                    uuid = it.uuid,
                    email = it.email,
                    username = it.username,
                    displayName = it.displayName,
                    avatarUrl = it.avatarUrl,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            tags = tags.map { it.toDomainTag(uuid) },
            taskCompletionLog = taskCompletionLogs.map { it.toDomainTaskCompletionLog() },
            assignees = assignees.map { assignee ->
                toDomainUser(
                    uuid = assignee.user.uuid,
                    email = assignee.user.email,
                    username = assignee.user.username,
                    displayName = assignee.user.displayName,
                    avatarUrl = assignee.user.avatarUrl,
                    createdAt = assignee.user.createdAt,
                    updatedAt = assignee.user.updatedAt,
                )
            },
            startDate = startDate?.toLocalDateOrNull(),
            dueDate = dueDate.toLocalDateOrNull(),
            projectId = projectId.toUuidOrNull(),
        )

        return getTaskStatus(domainTask, targetDate)
    }

    private fun PersonalTasksByDateRangeQuery.PersonalTasksByDateRange.toDomainTask(targetDate: LocalDate): Task {
        val domainTask = Task(
            uuid = Uuid.parse(uuid),
            title = title,
            description = description.orEmpty(),
            repetition = repetition ?: 0,
            type = type.toDomainTaskType(),
            status = status.toDomainTaskStatus(),
            priority = priority.toDomainTaskPriority(),
            creator = creator?.let {
                toDomainUser(
                    uuid = it.uuid,
                    email = it.email,
                    username = it.username,
                    displayName = it.displayName,
                    avatarUrl = it.avatarUrl,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            tags = tags.map { it.toDomainTag(uuid) },
            taskCompletionLog = taskCompletionLogs.map { it.toDomainTaskCompletionLog() },
            assignees = assignees.map { assignee ->
                toDomainUser(
                    uuid = assignee.user.uuid,
                    email = assignee.user.email,
                    username = assignee.user.username,
                    displayName = assignee.user.displayName,
                    avatarUrl = assignee.user.avatarUrl,
                    createdAt = assignee.user.createdAt,
                    updatedAt = assignee.user.updatedAt,
                )
            },
            startDate = startDate.toLocalDateOrNull(),
            dueDate = dueDate.toLocalDateOrNull(),
            projectId = projectId.toUuidOrNull(),
        )

        return getTaskStatus(domainTask, targetDate)
    }

    private fun toDomainUser(
        uuid: String,
        email: String,
        username: String,
        displayName: String,
        avatarUrl: String?,
        createdAt: String?,
        updatedAt: String?,
    ): User {
        return User(
            uuid = Uuid.parse(uuid),
            email = email,
            username = username,
            passwordHash = null,
            displayName = displayName,
            avatarUrl = avatarUrl,
            createdAt = createdAt.toLocalDateTimeOrNow(),
            updatedAt = updatedAt.toLocalDateTimeOrNow(),
        )
    }

    private fun PersonalTasksQuery.TaskCompletionLog.toDomainTaskCompletionLog(): TaskCompletionLog {
        return TaskCompletionLog(
            taskCompletionId = taskCompletionId.let(Uuid::parse),
            status = status.toDomainTaskStatus(),
            date = date.toLocalDateOrNull(),
            completedAt = completedAt.toLocalDateTimeOrNull(),
            taskId = taskId.let(Uuid::parse),
            userId = userId.let(Uuid::parse),
            userDisplayName = user.displayName,
            taskTitle = null,
        )
    }

    private fun PersonalTasksByDateRangeQuery.TaskCompletionLog.toDomainTaskCompletionLog(): TaskCompletionLog {
        return TaskCompletionLog(
            taskCompletionId = taskCompletionId.let(Uuid::parse),
            status = status.toDomainTaskStatus(),
            date = date.toLocalDateOrNull(),
            completedAt = completedAt.toLocalDateTimeOrNull(),
            taskId = taskId.let(Uuid::parse),
            userId = userId.let(Uuid::parse),
            userDisplayName = user.displayName,
            taskTitle = null,
        )
    }

    private fun PersonalTasksQuery.Tag.toDomainTag(parentTaskUuid: String): Tag {
        val resolvedWorkspaceId = workspaceId.toUuidOrNull()
        val resolvedOwnership = when (ownershipType) {
            GqlTagOwnershipType.PERSONAL -> TagOwnershipType.PERSONAL
            GqlTagOwnershipType.WORKSPACE -> TagOwnershipType.WORKSPACE
            else -> TagOwnershipType.PERSONAL
        }
        val targetLabelUuid = label.uuid
        val resolvedLabel = BuiltinLabels.find { builtin ->
            builtin.id.toString() == targetLabelUuid
        } ?: BuiltinLabels.first()

        return Tag(
            createdBy = createdBy.let(Uuid::parse),
            ownershipType = resolvedOwnership,
            workspaceId = if (resolvedOwnership == TagOwnershipType.WORKSPACE) resolvedWorkspaceId else null,
            taskIds = listOfNotNull(parentTaskUuid.toUuidOrNull()),
            uuid = Uuid.parse(uuid),
            name = name,
            color = color,
            label = resolvedLabel,
            createdAt = createdAt.toLocalDateTimeOrNow(),
            updatedAt = updatedAt.toLocalDateTimeOrNow(),
            creator = creator?.let {
                toDomainUser(
                    uuid = it.uuid,
                    email = it.email,
                    username = it.username,
                    displayName = it.displayName,
                    avatarUrl = it.avatarUrl,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            tasks = emptyList(),
        )
    }

    private fun PersonalTasksByDateRangeQuery.Tag.toDomainTag(parentTaskUuid: String): Tag {
        val resolvedWorkspaceId = workspaceId.toUuidOrNull()
        val resolvedOwnership = when (ownershipType) {
            GqlTagOwnershipType.PERSONAL -> TagOwnershipType.PERSONAL
            GqlTagOwnershipType.WORKSPACE -> TagOwnershipType.WORKSPACE
            else -> TagOwnershipType.PERSONAL
        }
        val targetLabelUuid = label.uuid
        val resolvedLabel = BuiltinLabels.find { builtin ->
            builtin.id.toString() == targetLabelUuid
        } ?: BuiltinLabels.first()

        return Tag(
            createdBy = createdBy.let(Uuid::parse),
            ownershipType = resolvedOwnership,
            workspaceId = if (resolvedOwnership == TagOwnershipType.WORKSPACE) resolvedWorkspaceId else null,
            taskIds = listOfNotNull(parentTaskUuid.toUuidOrNull()),
            uuid = Uuid.parse(uuid),
            name = name,
            color = color,
            label = resolvedLabel,
            createdAt = createdAt.toLocalDateTimeOrNow(),
            updatedAt = updatedAt.toLocalDateTimeOrNow(),
            creator = creator?.let {
                toDomainUser(
                    uuid = it.uuid,
                    email = it.email,
                    username = it.username,
                    displayName = it.displayName,
                    avatarUrl = it.avatarUrl,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            tasks = emptyList(),
        )
    }

    private fun GqlTaskType.toDomainTaskType(): TaskType =
        when (this) {
            GqlTaskType.PROJECT -> TaskType.PROJECT
            GqlTaskType.HABIT -> TaskType.HABIT
            else -> TaskType.HABIT
        }

    private fun GqlTaskStatus.toDomainTaskStatus(): TaskStatus =
        when (this) {
            GqlTaskStatus.CANCELLED -> TaskStatus.FAILED
            GqlTaskStatus.DONE -> TaskStatus.DONE
            GqlTaskStatus.IN_PROGRESS -> TaskStatus.IN_PROGRESS
            GqlTaskStatus.TODO -> TaskStatus.TODO
            GqlTaskStatus.FAILED -> TaskStatus.FAILED
            else -> TaskStatus.TODO
        }

    private fun String?.toDomainTaskStatus(): TaskStatus {
        return when (this?.uppercase()) {
            "DONE" -> TaskStatus.DONE
            "FAILED", "CANCELLED" -> TaskStatus.FAILED
            "IN_PROGRESS" -> TaskStatus.IN_PROGRESS
            else -> TaskStatus.TODO
        }
    }

    private fun GqlTaskPriority.toDomainTaskPriority(): TaskPriority =
        when (this) {
            GqlTaskPriority.LOW -> TaskPriority.LOW
            GqlTaskPriority.MEDIUM -> TaskPriority.MEDIUM
            GqlTaskPriority.HIGH -> TaskPriority.HIGH
            else -> TaskPriority.MEDIUM
        }
}

private fun String?.toLocalDateTimeOrNull(): LocalDateTime? {
    if (this.isNullOrBlank()) return null
    return runCatching { LocalDateTime.parse(this) }.getOrNull()
}
