@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.remote

import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.users_management.domain.entity.User
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
import com.example.se405.android.graphql.MarkTaskDoneMutation
import com.example.se405.android.graphql.MarkTaskWontDoMutation
import com.example.se405.android.graphql.type.MarkTaskDoneInput
import com.example.se405.android.graphql.type.TaskPriority as GqlTaskPriority
import com.example.se405.android.graphql.type.TaskStatus as GqlTaskStatus
import com.example.se405.android.graphql.type.TaskType as GqlTaskType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class TaskApiImpl(private val apolloClient: ApolloClient) : TaskApi {
    override suspend fun getTasks(userId: Uuid): Optional<List<Task>> {
        android.util.Log.d("TaskApiImpl", "getTasks: userId=$userId")
        return try {
            val response = apolloClient.query(GetTasksQuery(userId.toString())).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TaskApiImpl", "getTasks response errors: ${response.errors}")
                Optional.empty()
            } else {
                val tasks = response.data?.getTasks ?: run {
                    android.util.Log.w("TaskApiImpl", "getTasks response data is empty")
                    return Optional.empty()
                }
                android.util.Log.i("TaskApiImpl", "getTasks success: returned ${tasks.size} tasks")
                Optional.of(tasks.map(::toDomainTask))
            }
        } catch (e: Exception) {
            android.util.Log.e("TaskApiImpl", "getTasks exception", e)
            Optional.empty()
        }
    }

    override suspend fun createTask(task: Task, creatorId: Uuid?): Optional<Task> {
        android.util.Log.d("TaskApiImpl", "createTask: title=${task.title}, description=${task.description}")
        return try {
            val input = CreateTaskInput(
                title = task.title,
                description = GqlOptional.present(task.description),
                repetition = GqlOptional.present(task.repetition),
                type = task.type.toGqlTaskType(),
                status = task.status.toGqlTaskStatus(),
                priority = task.priority.toGqlTaskPriority(),
                creatorId = GqlOptional.present((creatorId ?: task.creator?.uuid)?.toString()),
                projectId = GqlOptional.present(task.projectId?.toString()),
                startDate = GqlOptional.present(task.startDate?.toString()),
                dueDate = GqlOptional.present(task.dueDate?.toString()),
                tagIds = GqlOptional.present(task.tags.map { it.uuid.toString() }),
                assigneeIds = GqlOptional.present(task.assignees.map { it.uuid.toString() }),
            )
            android.util.Log.d("TaskApiImpl", "createTask inputs: $input")
            val response = apolloClient.mutation(CreateTaskMutation(input)).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TaskApiImpl", "createTask response errors: ${response.errors}")
                return Optional.empty()
            }

            val created = response.data?.createTask ?: run {
                android.util.Log.e("TaskApiImpl", "createTask response data is empty")
                return Optional.empty()
            }
            android.util.Log.i("TaskApiImpl", "createTask success: uuid=${created.uuid}")
            Optional.of(
                Task(
                    uuid = Uuid.parse(created.uuid),
                    title = created.title,
                    description = created.description.orEmpty(),
                    repetition = created.repetition ?: task.repetition,
                    type = created.type.toDomainTaskType(),
                    status = created.status.toDomainTaskStatus(),
                    priority = created.priority.toDomainTaskPriority(),
                    creator = created.creator?.let {
                        toDomainUser(
                            uuid = it.uuid,
                            email = it.email,
                            username = it.username,
                            displayName = it.displayName,
                            avatarUrl = it.avatarUrl,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt
                        )
                    },
                    tags = task.tags,
                    taskCompletionLog = emptyList(),
                    assignees = created.assignees.map { assignee ->
                        toDomainUser(
                            uuid = assignee.user.uuid,
                            email = assignee.user.email,
                            username = assignee.user.username,
                            displayName = assignee.user.displayName,
                            avatarUrl = assignee.user.avatarUrl,
                            createdAt = "",
                            updatedAt = ""
                        )
                    },
                    startDate = created.startDate.toLocalDateTimeOrNow().toLocalDate(),
                    dueDate = created.dueDate.toLocalDateTimeOrNow().toLocalDate(),
                    projectId = created.projectId.toUuidOrNull(),
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("TaskApiImpl", "createTask exception", e)
            Optional.empty()
        }
    }

    override suspend fun updateTask(task: Task): Optional<Task> {
        android.util.Log.d("TaskApiImpl", "updateTask: uuid=${task.uuid}, title=${task.title}")
        return try {
            val input = UpdateTaskInput(
                uuid = task.uuid.toString(),
                title = GqlOptional.present(task.title),
                description = GqlOptional.present(task.description),
                repetition = GqlOptional.present(task.repetition),
                type = GqlOptional.present(task.type.toGqlTaskType()),
                status = GqlOptional.present(task.status.toGqlTaskStatus()),
                priority = GqlOptional.present(task.priority.toGqlTaskPriority()),
                projectId = GqlOptional.present(task.projectId?.toString()),
                startDate = GqlOptional.present(task.startDate?.toString()),
                dueDate = GqlOptional.present(task.dueDate?.toString()),
                tagIds = GqlOptional.present(task.tags.map { it.uuid.toString() }),
                assigneeIds = GqlOptional.present(task.assignees.map { it.uuid.toString() }),
            )
            android.util.Log.d("TaskApiImpl", "updateTask inputs: $input")
            val response = apolloClient.mutation(UpdateTaskMutation(input)).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TaskApiImpl", "updateTask response errors: ${response.errors}")
                return Optional.empty()
            }

            val updated = response.data?.updateTask ?: run {
                android.util.Log.e("TaskApiImpl", "updateTask response data is empty")
                return Optional.empty()
            }
            android.util.Log.i("TaskApiImpl", "updateTask success: uuid=${updated.uuid}")
            Optional.of(
                task.copy(
                    uuid = Uuid.parse(updated.uuid),
                    title = updated.title,
                    description = updated.description.orEmpty(),
                    repetition = updated.repetition ?: task.repetition,
                    type = updated.type.toDomainTaskType(),
                    status = updated.status.toDomainTaskStatus(),
                    priority = updated.priority.toDomainTaskPriority(),
                    projectId = updated.projectId.toUuidOrNull(),
                    startDate = updated.startDate.toLocalDateTimeOrNow().toLocalDate(),
                    dueDate = updated.dueDate.toLocalDateTimeOrNow().toLocalDate(),
                    assignees = updated.assignees.map { assignee ->
                        toDomainUser(
                            uuid = assignee.user.uuid,
                            email = assignee.user.email,
                            username = assignee.user.username,
                            displayName = assignee.user.displayName,
                            avatarUrl = assignee.user.avatarUrl,
                            createdAt = "",
                            updatedAt = ""
                        )
                    },
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("TaskApiImpl", "updateTask exception", e)
            Optional.empty()
        }
    }

    override suspend fun deleteTask(task: Task): Optional<Uuid> {
        android.util.Log.d("TaskApiImpl", "deleteTask: uuid=${task.uuid}")
        return try {
            val response = apolloClient.mutation(DeleteTaskMutation(task.uuid.toString())).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TaskApiImpl", "deleteTask response errors: ${response.errors}")
                return Optional.empty()
            }

            val deletedUuid = response.data?.deleteTask?.uuid ?: run {
                android.util.Log.e("TaskApiImpl", "deleteTask response data is empty")
                return Optional.empty()
            }
            android.util.Log.i("TaskApiImpl", "deleteTask success: deletedUuid=$deletedUuid")
            Optional.of(Uuid.parse(deletedUuid))
        } catch (e: Exception) {
            android.util.Log.e("TaskApiImpl", "deleteTask exception", e)
            Optional.empty()
        }
    }

    override suspend fun markTaskDone(taskId: Uuid, userId: Uuid, date: LocalDate): Optional<TaskCompletionLog> {
        android.util.Log.d("TaskApiImpl", "markTaskDone: taskId=$taskId, userId=$userId, date=$date")
        return try {
            val input = MarkTaskDoneInput(
                taskId = taskId.toString(),
                userId = userId.toString(),
                date = date.toString()
            )
            val response = apolloClient.mutation(MarkTaskDoneMutation(input)).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TaskApiImpl", "markTaskDone response errors: ${response.errors}")
                return Optional.empty()
            }

            val logged = response.data?.markTaskDone ?: run {
                android.util.Log.e("TaskApiImpl", "markTaskDone response data is empty")
                return Optional.empty()
            }
            android.util.Log.i("TaskApiImpl", "markTaskDone success: taskCompletionId=${logged.taskCompletionId}")
            Optional.of(
                TaskCompletionLog(
                    taskCompletionId = Uuid.parse(logged.taskCompletionId),
                    status = TaskStatus.DONE,
                    date = LocalDate.parse(logged.date),
                    completedAt = LocalDateTime.parse(logged.completedAt),
                    taskId = Uuid.parse(logged.taskId),
                    userId = Uuid.parse(logged.userId),
                    userDisplayName = null,
                    taskTitle = null
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("TaskApiImpl", "markTaskDone exception", e)
            Optional.empty()
        }
    }

    override suspend fun markTaskWontDo(taskId: Uuid, userId: Uuid, date: LocalDate): Optional<TaskCompletionLog> {
        android.util.Log.d("TaskApiImpl", "markTaskWontDo: taskId=$taskId, userId=$userId, date=$date")
        return try {
            val input = MarkTaskDoneInput(
                taskId = taskId.toString(),
                userId = userId.toString(),
                date = date.toString()
            )
            val response = apolloClient.mutation(MarkTaskWontDoMutation(input)).execute()
            if (!response.errors.isNullOrEmpty()) {
                android.util.Log.e("TaskApiImpl", "markTaskWontDo response errors: ${response.errors}")
                return Optional.empty()
            }

            val logged = response.data?.markTaskWontDo ?: run {
                android.util.Log.e("TaskApiImpl", "markTaskWontDo response data is empty")
                return Optional.empty()
            }
            android.util.Log.i("TaskApiImpl", "markTaskWontDo success: taskCompletionId=${logged.taskCompletionId}")
            Optional.of(
                TaskCompletionLog(
                    taskCompletionId = Uuid.parse(logged.taskCompletionId),
                    status = TaskStatus.FAILED,
                    date = LocalDate.parse(logged.date),
                    completedAt = LocalDateTime.parse(logged.completedAt),
                    taskId = Uuid.parse(logged.taskId),
                    userId = Uuid.parse(logged.userId),
                    userDisplayName = null,
                    taskTitle = null,
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("TaskApiImpl", "markTaskWontDo exception", e)
            Optional.empty()
        }
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
            creator = task.creator?.let {
                toDomainUser(
                    uuid = it.uuid,
                    email = it.email,
                    username = it.username,
                    displayName = it.displayName,
                    avatarUrl = it.avatarUrl,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            },
            tags = task.tags.map { it.toDomainTag(parentTaskUuid = task.uuid) },
            taskCompletionLog = task.taskCompletionLogs.map { it.toDomainTaskCompletionLog() },
            assignees = task.assignees.map { assignee ->
                toDomainUser(
                    uuid = assignee.user.uuid,
                    email = assignee.user.email,
                    username = assignee.user.username,
                    displayName = assignee.user.displayName,
                    avatarUrl = assignee.user.avatarUrl,
                    createdAt = "",
                    updatedAt = ""
                )
            },
            startDate = task.startDate.toLocalDateTimeOrNow().toLocalDate(),
            dueDate = task.dueDate.toLocalDateTimeOrNow().toLocalDate(),
            projectId = task.projectId.toUuidOrNull(),
        )
    }

    private fun GetTasksQuery.TaskCompletionLog.toDomainTaskCompletionLog(): TaskCompletionLog {
        return TaskCompletionLog(
            taskCompletionId = Uuid.parse(taskCompletionId),
            status = TaskStatus.DONE,
            date = LocalDate.parse(date),
            completedAt = LocalDateTime.parse(completedAt),
            taskId = Uuid.parse(taskId),
            userId = Uuid.parse(userId),
            userDisplayName = null,
            taskTitle = null,
        )
    }

    private fun GetTasksQuery.Tag.toDomainTag(parentTaskUuid: String): Tag {
        val resolvedWorkspaceId = workspaceId.toUuidOrNull()
        val resolvedOwnership = when {
            resolvedWorkspaceId == null -> TagOwnershipType.PERSONAL
            ownershipType.name == TagOwnershipType.WORKSPACE.name -> TagOwnershipType.WORKSPACE
            else -> TagOwnershipType.WORKSPACE
        }
        val targetLabelUuid = this.label.uuid
        val resolvedLabel = BuiltinLabels.find { builtin ->
            builtin.id.toString() == targetLabelUuid
        } ?: BuiltinLabels.first()

        return Tag(
            createdBy = ZERO_UUID,
            ownershipType = resolvedOwnership,
            workspaceId = if (resolvedOwnership == TagOwnershipType.WORKSPACE) resolvedWorkspaceId else null,
            taskIds = listOfNotNull(parentTaskUuid.toUuidOrNull()),
            uuid = Uuid.parse(uuid),
            name = name,
            color = color,
            label = resolvedLabel,
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

fun String?.toUuidOrNull(): Uuid? {
    if (this.isNullOrBlank()) return null
    return runCatching { Uuid.parse(this) }.getOrNull()
}