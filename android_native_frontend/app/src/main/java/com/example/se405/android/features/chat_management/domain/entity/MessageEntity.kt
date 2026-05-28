package com.example.se405.android.features.chat_management.domain.entity

import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ReplyMessageInfo @OptIn(ExperimentalUuidApi::class) constructor(
    val uuid: Uuid,
    val content: String,
    val senderName: String
)
@OptIn(ExperimentalUuidApi::class)
data class MessageEntity(
    val uuid: Uuid,
    val content: String,
    val conversationId: Uuid,
    val sender: User,
    val createdAt: LocalDateTime,
    val isOwnMessage: Boolean,
    val replyTo: ReplyMessageInfo? = null
) {
    init {
        require(content.isNotBlank()) { "Message content must not be blank" }
    }
}