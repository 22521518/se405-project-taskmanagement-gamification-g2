package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tags")
data class TagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val color: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_type", nullable = false)
    val ownershipType: TagOwnershipType,

    @Column(name = "workspace_id")
    val workspaceId: UUID? = null,

    /** UUID of the user who created this tag */
    @Column(name = "created_by", nullable = false)
    val createdBy: UUID,

    /**
     * References a HabitLabelEntity UUID.
     * Nullable — tags are not required to have a label.
     */
    @Column(name = "label_id")
    val labelId: UUID? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * Inverse side of Task.tags ManyToMany.
     * mappedBy points to the field name in TaskEntity.
     */
    @ManyToMany(mappedBy = "tags", fetch = FetchType.EAGER)
    val tasks: MutableList<TaskEntity> = mutableListOf(),
){
    override fun toString(): String {
        return "TagEntity(uuid=$uuid, name='$name', color=$color)"
    }

    override fun hashCode(): Int = uuid?.hashCode() ?: 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TagEntity

        if (color != other.color) return false
        if (uuid != other.uuid) return false
        if (name != other.name) return false
        if (ownershipType != other.ownershipType) return false
        if (workspaceId != other.workspaceId) return false
        if (createdBy != other.createdBy) return false
        if (labelId != other.labelId) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (tasks != other.tasks) return false

        return true
    }
}

enum class TagOwnershipType {
    WORKSPACE,
    PERSONAL,
}