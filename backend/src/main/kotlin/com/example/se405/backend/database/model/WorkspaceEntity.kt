package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "workspaces")
class WorkspaceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @Column(nullable = false)
    var name: String,


    @OneToMany(mappedBy = "workspace", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @OrderBy("createdAt DESC")
    var projects: List<ProjectEntity> = mutableListOf(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: UserEntity,

    @OneToMany(mappedBy = "workspace", fetch = FetchType.EAGER)
    var members: List<WorkspaceMemberEntity> = mutableListOf()
)