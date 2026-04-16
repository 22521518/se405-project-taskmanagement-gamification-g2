package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    val uuid: UUID,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Column(name = "display_name", nullable = false)
    val displayName: String,

    @Column(name = "avatar_url")
    val avatarUrl: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
