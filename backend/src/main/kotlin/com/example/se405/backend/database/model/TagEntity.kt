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
)

enum class TagOwnershipType {
    WORKSPACE,
    PERSONAL,
}