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
){
    override fun toString(): String {
        return "ConversationParticipantEntity(uuid=$uuid)"
    }

    override fun hashCode(): Int = uuid?.hashCode() ?: 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConversationParticipantEntity

        if (uuid != other.uuid) return false
        if (conversation != other.conversation) return false
        if (user != other.user) return false

        return true
    }
}