package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MessageRepository : JpaRepository<MessageEntity, UUID> {
    // Lấy tin nhắn theo ID của Conversation
    fun findByConversationUuidOrderByCreatedAtAsc(conversationUuid: UUID): List<MessageEntity>
    fun findByConversationUuidAndIsPinnedTrueOrderByPinnedAtAsc(conversationId: UUID): List<MessageEntity>
    fun findByConversationUuidAndTypeInAndIsRevokedFalseOrderByCreatedAtDesc(
        conversationId: UUID,
        types: List<String>
    ): List<MessageEntity>

    fun findByConversationUuidAndTypeAndContentContainingIgnoreCaseAndIsRevokedFalseOrderByCreatedAtDesc(
        conversationUuid: UUID,
        type: String,
        keyword: String
    ): List<MessageEntity>

    fun findByConversationUuidAndContentContainingIgnoreCaseAndIsRevokedFalseOrderByCreatedAtDesc(
        conversationUuid: UUID,
        keyword: String
    ): List<MessageEntity>
}