package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Embeddable
data class ProjectMemberId(
    @Column(name = "project_id")
    val projectId: UUID,

    @Column(name = "workspace_id")
    val workspaceId: UUID,

    @Column(name = "user_id")
    val userId: UUID
) : Serializable

@Entity
@Table(name = "project_members")
class ProjectMemberEntity(

    @EmbeddedId
    val id: ProjectMemberId,

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    var project: ProjectEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns(
        JoinColumn(name = "workspace_id", referencedColumnName = "workspace_id", insertable = false, updatable = false),
        JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    )
    var workspaceMember: WorkspaceMemberEntity,


    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    var user: UserEntity,

    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now()
)