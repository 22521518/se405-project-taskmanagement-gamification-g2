package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "conversation_participants")
data class ConversationParticipantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_uuid", nullable = false)
    val conversation: ConversationEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", nullable = false)
    val user: UserEntity
)