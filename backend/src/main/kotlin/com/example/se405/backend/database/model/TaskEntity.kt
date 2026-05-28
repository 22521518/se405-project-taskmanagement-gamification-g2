package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "tasks")
data class TaskEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    val repetition: Int? = null,

    /** "PROJECT" or "HABIT" — stored as string for GraphQL compatibility */
    @Column(nullable = false)
    val type: String,

    @Column(nullable = false)
    val status: String,

    @Column(nullable = false)
    val priority: String,

    @Column(name = "creator_id")
    val creatorId: UUID? = null,

    /**
     * Only relevant when type = "PROJECT".
     * When projectId is null, startDate/dueDate should also be null.
     */
    @Column(name = "project_id")
    val projectId: UUID? = null,

    /**
     * Only set for PROJECT-type tasks (projectId != null).
     * Stored as ISO date string (yyyy-MM-dd).
     */
    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    /** Only set for PROJECT-type tasks (projectId != null). */
    @Column(name = "due_date")
    val dueDate: LocalDate? = null,

    /**
     * Many-to-many relation with tags via junction table task_tags.
     * Fetched lazily to avoid N+1 issues.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_tags",
        joinColumns = [JoinColumn(name = "task_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")],
    )
    val tags: MutableList<TagEntity> = mutableListOf(),
){
    override fun toString(): String {
        return "TaskEntity(uuid=$uuid, title='$title', status=$status, type=$type)"
    }
    override fun hashCode(): Int = uuid?.hashCode() ?: 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskEntity

        if (repetition != other.repetition) return false
        if (uuid != other.uuid) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (type != other.type) return false
        if (status != other.status) return false
        if (priority != other.priority) return false
        if (creatorId != other.creatorId) return false
        if (projectId != other.projectId) return false
        if (startDate != other.startDate) return false
        if (dueDate != other.dueDate) return false
        if (tags != other.tags) return false

        return true
    }
}
