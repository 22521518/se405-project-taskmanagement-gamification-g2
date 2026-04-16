package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.HabitLabelEntity
import com.example.se405.backend.database.model.TagEntity
import com.example.se405.backend.database.model.TagOwnershipType
import com.example.se405.backend.database.model.TaskEntity
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.repository.HabitLabelRepository
import com.example.se405.backend.database.repository.TagRepository
import com.example.se405.backend.database.repository.UserRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.time.LocalDateTime
import java.util.UUID

@Controller
class TagController(
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository,
    private val habitLabelRepository: HabitLabelRepository,
) {

    @QueryMapping
    fun getTagsByUser(@Argument userId: UUID): List<TagEntity> {
        // Returns personal tags created by this user + workspace tags in their workspaces
        return tagRepository.findByCreatedBy(userId)
    }

    @QueryMapping
    fun getTagsByWorkspace(@Argument workspaceId: UUID): List<TagEntity> =
        tagRepository.findByWorkspaceId(workspaceId)

    @QueryMapping
    fun getTagsForTaskOwnership(@Argument taskId: UUID): List<TagEntity> =
        tagRepository.findByTaskId(taskId)

    @QueryMapping
    fun getHabitLabels(): List<HabitLabelEntity> = habitLabelRepository.findAll()

    // ── Resolve nested fields ─────────────────────────────────────────────────

    /** Resolve the creator User object for a Tag */
    @SchemaMapping(typeName = "Tag", field = "creator")
    fun tagCreator(tag: TagEntity): UserEntity? =
        userRepository.findById(tag.createdBy).orElse(null)

    /** Resolve the HabitLabel object for a Tag */
    @SchemaMapping(typeName = "Tag", field = "label")
    fun tagLabel(tag: TagEntity): HabitLabelEntity? =
        tag.labelId?.let { habitLabelRepository.findById(it).orElse(null) }

    /** Resolve tasks list for a Tag */
    @SchemaMapping(typeName = "Tag", field = "tasks")
    fun tagTasks(tag: TagEntity): List<TaskEntity> = tag.tasks

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    fun createTag(@Argument input: CreateTagInput): TagEntity {
        val tag = TagEntity(
            name = input.name,
            color = input.color,
            ownershipType = input.ownershipType,
            workspaceId = input.workspaceId,
            createdBy = input.createdBy,
            labelId = input.labelId,
        )
        return tagRepository.save(tag)
    }

    @MutationMapping
    fun updateTag(@Argument input: UpdateTagInput): TagEntity {
        val existing = tagRepository.findById(input.uuid)
            .orElseThrow { RuntimeException("Tag not found") }

        val updated = existing.copy(
            name = input.name ?: existing.name,
            color = input.color ?: existing.color,
            ownershipType = input.ownershipType ?: existing.ownershipType,
            workspaceId = input.workspaceId ?: existing.workspaceId,
            labelId = input.labelId ?: existing.labelId,
            updatedAt = LocalDateTime.now(),
        )
        return tagRepository.save(updated)
    }

    @MutationMapping
    fun deleteTag(@Argument uuid: UUID): TagEntity {
        val existing = tagRepository.findById(uuid)
            .orElseThrow { RuntimeException("Tag not found") }
        tagRepository.delete(existing)
        return existing
    }
}

data class CreateTagInput(
    val name: String,
    val color: Int,
    val ownershipType: TagOwnershipType,
    val workspaceId: UUID? = null,
    val createdBy: UUID,
    val labelId: UUID? = null,
)

data class UpdateTagInput(
    val uuid: UUID,
    val name: String? = null,
    val color: Int? = null,
    val ownershipType: TagOwnershipType? = null,
    val workspaceId: UUID? = null,
    val labelId: UUID? = null,
)
