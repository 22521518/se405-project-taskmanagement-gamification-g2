package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "workspaces")
data class WorkspaceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @Column(nullable = false)
    val name: String,
)
