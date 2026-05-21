package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.TagEntity
import com.example.se405.backend.database.model.TaskCompletionLogEntity
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.repository.TagRepository
import com.example.se405.backend.database.repository.TaskCompletionLogRepository
import com.example.se405.backend.database.repository.TaskRepository
import com.example.se405.backend.database.repository.UserRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Controller
class TaskController(
    private val taskRepository: TaskRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository,
    private val taskCompletionLogRepository: TaskCompletionLogRepository,
) {

    @QueryMapping
    fun getTasks(@Argument userId: UUID) = taskRepository.findByCreatorId(userId)

    @QueryMapping
    fun getTaskCompletionLogs(@Argument taskId: UUID) =
        taskCompletionLogRepository.findByTaskId(taskId)

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
        if(task.uuid == null) return emptyList()
        return taskCompletionLogRepository.findByTaskId(task.uuid);
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

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    fun createTask(@Argument input: CreateTaskInput): com.example.se405.backend.database.model.TaskEntity {
        val tags = input.tagIds
            ?.mapNotNull { tagRepository.findById(it).orElse(null) }
            ?.toMutableList() ?: mutableListOf()

        val task = com.example.se405.backend.database.model.TaskEntity(
            title = input.title,
            description = input.description,
            type = input.type,
            status = input.status,
            priority = input.priority,
            creatorId = input.creatorId,
            projectId = input.projectId,
            startDate = input.startDate?.let { LocalDate.parse(it) },
            dueDate = input.dueDate?.let { LocalDate.parse(it) },
            repetition = input.repetition,
            tags = tags,
        )
        return taskRepository.save(task)
    }

    @MutationMapping
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
        )
        return taskCompletionLogRepository.save(log)
    }

    @MutationMapping
    fun markTaskWontDo(@Argument input: MarkTaskDoneInput): TaskCompletionLogEntity {
        // Same shape as markTaskDone — caller passes appropriate status if needed
        val log = TaskCompletionLogEntity(
            taskId = input.taskId,
            userId = input.userId,
            date = LocalDate.parse(input.date),
            completedAt = LocalDateTime.now(),
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
)

data class MarkTaskDoneInput(
    val taskId: UUID,
    val userId: UUID,
    /** ISO date string for the tracked date (yyyy-MM-dd) */
    val date: String,
)
