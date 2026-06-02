package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

/** Composite PK for workspace_members table */@Embeddable
data class WorkspaceMemberId(
    @Column(name = "workspace_id")
    val workspaceId: UUID,

    @Column(name = "user_id")
    val userId: UUID
) : Serializable

@Entity
@Table(name = "workspace_members")
class WorkspaceMemberEntity(

    @EmbeddedId
    val id: WorkspaceMemberId,

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("workspaceId")
    @JoinColumn(name = "workspace_id")
    var workspace: WorkspaceEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    var user: UserEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: WorkspaceRole,

    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now()
) {
    val userId: UUID get() = id.userId
    val workspaceId: UUID get() = id.workspaceId
}

enum class WorkspaceRole {
    OWNER,
    MEMBER,
}
