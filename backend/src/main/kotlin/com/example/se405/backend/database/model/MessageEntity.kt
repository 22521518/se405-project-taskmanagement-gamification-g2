package com.example.se405.backend.database.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "messages")
data class MessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_uuid", nullable = false)
    val conversation: ConversationEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_uuid", nullable = false)
    val sender: UserEntity
){
    val conversationId: String
        get() = conversation.uuid.toString()

    val senderId: String
        get() = sender.uuid.toString()

    override fun toString(): String {
        return "MessageEntity(uuid=$uuid, content='$content', createdAt=$createdAt)"
    }

    override fun hashCode(): Int = uuid?.hashCode() ?: 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageEntity

        if (uuid != other.uuid) return false
        if (content != other.content) return false
        if (createdAt != other.createdAt) return false
        if (conversation != other.conversation) return false
        if (sender != other.sender) return false
        if (conversationId != other.conversationId) return false
        if (senderId != other.senderId) return false

        return true
    }
}