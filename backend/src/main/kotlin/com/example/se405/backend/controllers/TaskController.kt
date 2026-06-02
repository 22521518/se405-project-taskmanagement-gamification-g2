package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.TagEntity
import com.example.se405.backend.database.model.TaskAssigneeEntity
import com.example.se405.backend.database.model.TaskCompletionLogEntity
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.repository.TagRepository
import com.example.se405.backend.database.repository.TaskCompletionLogRepository
import com.example.se405.backend.database.repository.TaskRepository
import com.example.se405.backend.database.repository.UserRepository
import com.example.se405.backend.services.PersonalTaskService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Controller
class TaskController(
    private val taskRepository: TaskRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository,
    private val taskCompletionLogRepository: TaskCompletionLogRepository,
    private val personalTaskService: PersonalTaskService,
) {

    @QueryMapping
    fun getTasks(@Argument userId: UUID) =
        taskRepository.findTaskResponsibilitiesByUserIdWithAssignees(userId)
//        taskRepository.findByCreatorId(userId)

    @QueryMapping
    fun getTaskCompletionLogs(@Argument taskId: UUID) =
        taskCompletionLogRepository.findByTaskId(taskId)

    @QueryMapping
    fun personalTasks(@Argument userId: UUID) =
        personalTaskService.getPersonalTasks(userId)

    @QueryMapping
    fun personalTasksByDateRange(
        @Argument userId: UUID,
        @Argument from: LocalDate,
        @Argument to: LocalDate,
    ) = personalTaskService.getPersonalTasksByDateRange(userId, from, to)

    // ── Resolve nested fields ─────────────────────────────────────────────────

    /** Resolve the creator User object from a Task's creatorId */
    @SchemaMapping(typeName = "Task", field = "creator")
    fun taskCreator(task: com.example.se405.backend.database.model.TaskEntity): UserEntity? =
        task.creatorId?.let { userRepository.findById(it).orElse(null) }

    /** Resolve tags list for a Task */
    @SchemaMapping(typeName = "Task", field = "tags")
    fun taskTags(task: com.example.se405.backend.database.model.TaskEntity): List<TagEntity> =
        task.tags

    /** Resolve taskCompletionLogs list for a Task */
    @SchemaMapping(typeName = "Task", field = "taskCompletionLogs")
    fun taskCompletionLogs(
        task: com.example.se405.backend.database.model.TaskEntity,
    ): List<TaskCompletionLogEntity> {
        return taskCompletionLogRepository.findByTaskId(task.uuid!!)
    }

    /** Resolve assignees list for a Task */
    @SchemaMapping(typeName = "Task", field = "assignees")
    fun taskAssignees(
        task: com.example.se405.backend.database.model.TaskEntity,
    ): List<TaskAssigneeEntity> {
        return task.assignees
    }

    /** Resolve user for a TaskCompletionLog */
    @SchemaMapping(typeName = "TaskCompletionLog", field = "user")
    fun completionLogUser(log: TaskCompletionLogEntity): UserEntity? =
        userRepository.findById(log.userId).orElse(null)

    /** Resolve task for a TaskCompletionLog */
    @SchemaMapping(typeName = "TaskCompletionLog", field = "task")
    fun completionLogTask(
        log: TaskCompletionLogEntity,
    ) = taskRepository.findById(log.taskId).orElse(null)
    
    @SchemaMapping(typeName = "TaskAssignee", field = "user")
    fun taskAssigneeUser(taskAssignee: TaskAssigneeEntity) = taskAssignee.user

    // taskId/userId live in the embedded composite key (TaskAssigneeId), not as
    // direct properties, so the default property fetcher can't find them and returns
    // null on these non-null ID! fields. That null bubbles up through assignees ->
    // task -> tasks -> project -> projects, nulling the whole getWorkspace result
    // ("workspace not found"), and breaks createTask's returned assignees. Resolve
    // them explicitly, mirroring the WorkspaceMember/ProjectMember id-field mappings.
    @SchemaMapping(typeName = "TaskAssignee", field = "taskId")
    fun taskAssigneeTaskId(taskAssignee: TaskAssigneeEntity): UUID = taskAssignee.id.taskId

    @SchemaMapping(typeName = "TaskAssignee", field = "userId")
    fun taskAssigneeUserId(taskAssignee: TaskAssigneeEntity): UUID = taskAssignee.id.userId

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    @Transactional
    fun createTask(@Argument input: CreateTaskInput): com.example.se405.backend.database.model.TaskEntity {
        val username = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication?.principal as? String
        val creatorUser = username?.let { userRepository.findByUsername(it) }
        val finalCreatorId = creatorUser?.uuid ?: input.creatorId

        // Resolve only the tags that actually exist for this workspace/user; unknown
        // ids are silently skipped so a stale tag id can't fail the whole mutation.
        val tags = input.tagIds
            ?.mapNotNull { tagRepository.findById(it).orElse(null) }
            ?.toMutableList() ?: mutableListOf()

        // First save assigns the generated uuid, which the assignee composite id needs.
        // Running inside a single @Transactional keeps `saved` managed, so adding
        // assignees afterwards flushes via dirty-checking — no detached re-merge.
        val saved = taskRepository.save(
            com.example.se405.backend.database.model.TaskEntity(
                title = input.title,
                description = input.description,
                type = input.type,
                status = input.status,
                priority = input.priority,
                creatorId = finalCreatorId,
                projectId = input.projectId,
                startDate = input.startDate?.let { LocalDate.parse(it) },
                dueDate = input.dueDate?.let { LocalDate.parse(it) },
                repetition = input.repetition,
                tags = tags,
            )
        )

        if (!input.assigneeIds.isNullOrEmpty()) {
            // Distinct guards against a duplicate assignee id producing a duplicate
            // composite primary key in task_assignee.
            input.assigneeIds.distinct().forEach { userId ->
                val user = userRepository.findById(userId).orElse(null)
                if (user != null) {
                    saved.assignees.add(
                        com.example.se405.backend.database.model.TaskAssigneeEntity(
                            id = com.example.se405.backend.database.model.TaskAssigneeId(taskId = saved.uuid!!, userId = userId),
                            task = saved,
                            user = user
                        )
                    )
                }
            }
        }

        return saved
    }

    @MutationMapping
    @Transactional
    fun updateTask(@Argument input: UpdateTaskInput): com.example.se405.backend.database.model.TaskEntity {
        val existing = taskRepository.findById(input.uuid)
            .orElseThrow { RuntimeException("Task not found") }

        val updatedTags = input.tagIds
            ?.mapNotNull { tagRepository.findById(it).orElse(null) }
            ?.toMutableList() ?: existing.tags

        val updated = existing.copy(
            title = input.title ?: existing.title,
            description = input.description ?: existing.description,
            type = input.type ?: existing.type,
            status = input.status ?: existing.status,
            priority = input.priority ?: existing.priority,
            projectId = input.projectId ?: existing.projectId,
            startDate = input.startDate?.let { LocalDate.parse(it) } ?: existing.startDate,
            dueDate = input.dueDate?.let { LocalDate.parse(it) } ?: existing.dueDate,
            repetition = input.repetition ?: existing.repetition,
            tags = updatedTags,
        )

        if (input.assigneeIds != null) {
            updated.assignees.clear()
            input.assigneeIds.forEach { userId ->
                val user = userRepository.findById(userId).orElse(null)
                if (user != null) {
                    updated.assignees.add(
                        com.example.se405.backend.database.model.TaskAssigneeEntity(
                            id = com.example.se405.backend.database.model.TaskAssigneeId(taskId = updated.uuid!!, userId = userId),
                            task = updated,
                            user = user
                        )
                    )
                }
            }
        }

        return taskRepository.save(updated)
    }

    @MutationMapping
    fun deleteTask(@Argument uuid: UUID): com.example.se405.backend.database.model.TaskEntity {
        val existing = taskRepository.findById(uuid)
            .orElseThrow { RuntimeException("Task not found") }
        taskRepository.delete(existing)
        return existing
    }

    @MutationMapping
    fun markTaskDone(@Argument input: MarkTaskDoneInput): TaskCompletionLogEntity {
        val log = TaskCompletionLogEntity(
            taskId = input.taskId,
            userId = input.userId,
            date = LocalDate.parse(input.date),
            completedAt = LocalDateTime.now(),
            status = "DONE",
        )
        return taskCompletionLogRepository.save(log)
    }

    @MutationMapping
    fun markTaskWontDo(@Argument input: MarkTaskDoneInput): TaskCompletionLogEntity {
        val log = TaskCompletionLogEntity(
            taskId = input.taskId,
            userId = input.userId,
            date = LocalDate.parse(input.date),
            completedAt = LocalDateTime.now(),
            status = "FAILED"
        )
        return taskCompletionLogRepository.save(log)
    }
}

data class CreateTaskInput(
    val title: String,
    val description: String? = null,
    val type: String,
    val status: String,
    val priority: String,
    val repetition: Int? = null,
    val creatorId: UUID? = null,
    val projectId: UUID? = null,
    /** ISO date string (yyyy-MM-dd). Only for PROJECT type (projectId != null). */
    val startDate: String? = null,
    val dueDate: String? = null,
    val tagIds: List<UUID>? = null,
    val assigneeIds: List<UUID>? = null,
)

data class UpdateTaskInput(
    val uuid: UUID,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val repetition: Int? = null,
    val projectId: UUID? = null,
    val startDate: String? = null,
    val dueDate: String? = null,
    val tagIds: List<UUID>? = null,
    val assigneeIds: List<UUID>? = null,
)

data class MarkTaskDoneInput(
    val taskId: UUID,
    val userId: UUID,
    /** ISO date string for the tracked date (yyyy-MM-dd) */
    val date: String,
)
