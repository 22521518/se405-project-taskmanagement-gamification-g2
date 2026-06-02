package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.util.UUID

enum class ConversationType {
    DIRECT, GROUP, TASK
}

@Entity
@Table(name = "conversations")
data class ConversationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ConversationType,

    @Column(name = "name", nullable = true)
    val name: String? = null,

    // Trỏ về Task nếu type = TASK (Nullable)
    @Column(name = "task_uuid", nullable = true)
    val taskUuid: UUID? = null,

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "conversation_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    val messages: List<MessageEntity> = emptyList(),

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val participantLinks: List<ConversationParticipantEntity> = emptyList()
){
    val participants: List<UserEntity>
        get() = participantLinks.map { it.user }

    val lastMessage: MessageEntity? get() = messages.maxByOrNull { it.createdAt }

    override fun toString(): String {
        return "ConversationEntity(uuid=$uuid, type=$type, name=$name)"
    }

    override fun hashCode(): Int = uuid?.hashCode() ?: 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConversationEntity

        if (uuid != other.uuid) return false
        if (type != other.type) return false
        if (name != other.name) return false
        if (taskUuid != other.taskUuid) return false
        if (messages != other.messages) return false
        if (participantLinks != other.participantLinks) return false
        if (participants != other.participants) return false
        if (lastMessage != other.lastMessage) return false

        return true
    }
}