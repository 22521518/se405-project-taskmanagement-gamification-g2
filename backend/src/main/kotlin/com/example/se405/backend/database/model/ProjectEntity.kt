package com.example.se405.backend.database.model

import jakarta.persistence.*
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.UUID

@GraphQlTester.Entity
@Table(name = "projects")
data class ProjectEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @Column(nullable = false)
    val name: String,

    @Column(name = "workspace_id", nullable = false)
    val workspaceId: UUID,
)
