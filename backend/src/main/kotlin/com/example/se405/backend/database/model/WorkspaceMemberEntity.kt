package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

/** Composite PK for workspace_members table */
data class WorkspaceMemberId(
    val workspaceId: UUID = UUID.randomUUID(),
    val userId: UUID = UUID.randomUUID(),
) : Serializable

@Entity
@Table(name = "workspace_members")
@IdClass(WorkspaceMemberId::class)
data class WorkspaceMemberEntity(
    @Id
    @Column(name = "workspace_id", nullable = false)
    val workspaceId: UUID,

    @Id
    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: WorkspaceRole,

    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),
)

enum class WorkspaceRole {
    OWNER,
    MEMBER,
}
