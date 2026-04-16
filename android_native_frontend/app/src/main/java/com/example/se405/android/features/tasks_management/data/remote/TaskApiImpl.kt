@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.remote

import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional as GqlOptional
import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.graphql.GetTasksQuery
import com.example.se405.android.graphql.CreateTaskMutation
import com.example.se405.android.graphql.type.CreateTaskInput
import com.example.se405.android.graphql.UpdateTaskMutation
import com.example.se405.android.graphql.type.UpdateTaskInput
import com.example.se405.android.graphql.DeleteTaskMutation
import com.example.se405.android.graphql.type.TaskPriority as GqlTaskPriority
import com.example.se405.android.graphql.type.TaskStatus as GqlTaskStatus
import com.example.se405.android.graphql.type.TaskType as GqlTaskType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class TaskApiImpl(private val apolloClient: ApolloClient) : TaskApi {
    override suspend fun getTasks(userId: Uuid): Optional<List<Task>> {
        val response = apolloClient.query(GetTasksQuery(userId.toString())).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val tasks = response.data?.getTasks ?: return Optional.empty()
        return Optional.of(tasks.map(::toDomainTask))
    }

    override suspend fun createTask(task: Task): Optional<Task> {
        val input = CreateTaskInput(
            title = task.title,
            description = GqlOptional.present(task.description),
            type = task.type.toGqlTaskType(),
            status = task.status.toGqlTaskStatus(),
            priority = task.priority.toGqlTaskPriority(),
            creatorId = GqlOptional.present(task.creator?.uuid?.toString()),
            projectId = GqlOptional.present(task.projectId?.toString()),
            startDate = GqlOptional.present(task.startDate?.toString()),
            dueDate = GqlOptional.present(task.dueDate?.toString()),
            tagIds = GqlOptional.present(task.tags.map { it.uuid.toString() }),
        )
        val response = apolloClient.mutation(CreateTaskMutation(input)).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val created = response.data?.createTask ?: return Optional.empty()
        return Optional.of(
            Task(
                uuid = Uuid.parse(created.uuid),
                title = created.title,
                description = created.description.orEmpty(),
                repetition = created.repetition ?: task.repetition,
                type = created.type.toDomainTaskType(),
                status = created.status.toDomainTaskStatus(),
                priority = created.priority.toDomainTaskPriority(),
                creator = null,
                tags = emptyList(),
                taskCompletionLog = emptyList(),
                startDate = created.startDate.toLocalDateOrNull(),
                dueDate = created.dueDate.toLocalDateOrNull(),
                projectId = created.projectId.toUuidOrNull(),
            )
        )
    }

    override suspend fun updateTask(task: Task): Optional<Task> {
        val input = UpdateTaskInput(
            uuid = task.uuid.toString(),
            title = GqlOptional.present(task.title),
            description = GqlOptional.present(task.description),
            type = GqlOptional.present(task.type.toGqlTaskType()),
            status = GqlOptional.present(task.status.toGqlTaskStatus()),
            priority = GqlOptional.present(task.priority.toGqlTaskPriority()),
            projectId = GqlOptional.present(task.projectId?.toString()),
            startDate = GqlOptional.present(task.startDate?.toString()),
            dueDate = GqlOptional.present(task.dueDate?.toString()),
            tagIds = GqlOptional.present(task.tags.map { it.uuid.toString() }),
        )
        val response = apolloClient.mutation(UpdateTaskMutation(input)).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val updated = response.data?.updateTask ?: return Optional.empty()
        return Optional.of(
            task.copy(
                uuid = Uuid.parse(updated.uuid),
                title = updated.title,
                description = updated.description.orEmpty(),
                repetition = updated.repetition ?: task.repetition,
                type = updated.type.toDomainTaskType(),
                status = updated.status.toDomainTaskStatus(),
                priority = updated.priority.toDomainTaskPriority(),
                projectId = updated.projectId.toUuidOrNull(),
                startDate = updated.startDate.toLocalDateOrNull(),
                dueDate = updated.dueDate.toLocalDateOrNull(),
            )
        )
    }

    override suspend fun deleteTask(task: Task): Optional<Uuid> {
        val response = apolloClient.mutation(DeleteTaskMutation(task.uuid.toString())).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val deletedUuid = response.data?.deleteTask?.uuid ?: return Optional.empty()
        return Optional.of(Uuid.parse(deletedUuid))
    }

    private fun toDomainTask(task: GetTasksQuery.GetTask): Task {
        return Task(
            uuid = Uuid.parse(task.uuid),
            title = task.title,
            description = task.description.orEmpty(),
            repetition = task.repetition ?: 0,
            type = task.type.toDomainTaskType(),
            status = task.status.toDomainTaskStatus(),
            priority = task.priority.toDomainTaskPriority(),
            creator = null,
            tags = task.tags.map { it.toDomainTag(parentTaskUuid = task.uuid) },
            taskCompletionLog = task.taskCompletionLogs.map { it.toDomainTaskCompletionLog() },
            startDate = task.startDate.toLocalDateOrNull(),
            dueDate = task.dueDate.toLocalDateOrNull(),
            projectId = task.projectId.toUuidOrNull(),
        )
    }

    private fun GetTasksQuery.TaskCompletionLog.toDomainTaskCompletionLog(): TaskCompletionLog {
        return TaskCompletionLog(
            taskCompletionId = Uuid.parse(taskCompletionId),
            status = TaskStatus.DONE,
            date = LocalDate.parse(date),
            completedAt = LocalDateTime.parse(completedAt),
            task = Uuid.parse(taskId),
            user = Uuid.parse(userId),
        )
    }

    private fun GetTasksQuery.Tag.toDomainTag(parentTaskUuid: String): Tag {
        val resolvedWorkspaceId = workspaceId.toUuidOrNull()
        val resolvedOwnership = when {
            resolvedWorkspaceId == null -> TagOwnershipType.PERSONAL
            ownershipType.name == TagOwnershipType.WORKSPACE.name -> TagOwnershipType.WORKSPACE
            else -> TagOwnershipType.WORKSPACE
        }

        return Tag(
            createdBy = ZERO_UUID,
            ownershipType = resolvedOwnership,
            workspaceId = if (resolvedOwnership == TagOwnershipType.WORKSPACE) resolvedWorkspaceId else null,
            taskIds = listOfNotNull(parentTaskUuid.toUuidOrNull()),
            uuid = Uuid.parse(uuid),
            name = name,
            color = color,
            label = BuiltinLabels.first(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            creator = null,
            tasks = emptyList(),
        )
    }

    private fun GqlTaskType.toDomainTaskType(): TaskType =
        when (this) {
            GqlTaskType.PROJECT -> TaskType.PROJECT
            GqlTaskType.HABIT -> TaskType.HABIT
            else -> TaskType.HABIT
        }

    private fun TaskType.toGqlTaskType(): GqlTaskType =
        when (this) {
            TaskType.PROJECT -> GqlTaskType.PROJECT
            TaskType.HABIT -> GqlTaskType.HABIT
        }

    private fun GqlTaskStatus.toDomainTaskStatus(): TaskStatus =
        when (this) {
            GqlTaskStatus.CANCELLED -> TaskStatus.FAILED
            GqlTaskStatus.DONE -> TaskStatus.DONE
            GqlTaskStatus.IN_PROGRESS -> TaskStatus.IN_PROGRESS
            GqlTaskStatus.TODO -> TaskStatus.TODO
            else -> TaskStatus.TODO
        }

    private fun TaskStatus.toGqlTaskStatus(): GqlTaskStatus =
        when (this) {
            TaskStatus.TODO -> GqlTaskStatus.TODO
            TaskStatus.IN_PROGRESS -> GqlTaskStatus.IN_PROGRESS
            TaskStatus.DONE -> GqlTaskStatus.DONE
            TaskStatus.FAILED -> GqlTaskStatus.CANCELLED
        }

    private fun GqlTaskPriority.toDomainTaskPriority(): TaskPriority =
        when (this) {
            GqlTaskPriority.LOW -> TaskPriority.LOW
            GqlTaskPriority.MEDIUM -> TaskPriority.MEDIUM
            GqlTaskPriority.HIGH -> TaskPriority.HIGH
            else -> TaskPriority.MEDIUM
        }

    private fun TaskPriority.toGqlTaskPriority(): GqlTaskPriority =
        when (this) {
            TaskPriority.LOW -> GqlTaskPriority.LOW
            TaskPriority.MEDIUM -> GqlTaskPriority.MEDIUM
            TaskPriority.HIGH -> GqlTaskPriority.HIGH
        }

    private fun String?.toLocalDateOrNull(): LocalDate? {
        if (this.isNullOrBlank()) return null
        return runCatching { LocalDate.parse(this) }.getOrNull()
    }

    private fun String?.toUuidOrNull(): Uuid? {
        if (this.isNullOrBlank()) return null
        return runCatching { Uuid.parse(this) }.getOrNull()
    }

    private companion object {
        val ZERO_UUID: Uuid = Uuid.fromLongs(0L, 0L)
    }
}
