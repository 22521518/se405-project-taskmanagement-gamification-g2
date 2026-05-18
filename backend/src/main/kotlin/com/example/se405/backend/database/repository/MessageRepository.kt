package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MessageRepository : JpaRepository<MessageEntity, UUID> {
    // Lấy tin nhắn theo ID của Conversation
    fun findByConversationUuidOrderByCreatedAtAsc(conversationUuid: UUID): List<MessageEntity>
}