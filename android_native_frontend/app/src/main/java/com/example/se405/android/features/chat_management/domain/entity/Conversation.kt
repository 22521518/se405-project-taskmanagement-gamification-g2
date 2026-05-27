package com.example.se405.android.features.chat_management.domain.entity

import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDateTime

data class LastMessageInfo(
    val content: String,
    val createdAt: LocalDateTime,
    val senderId: String,
    val senderName: String
)
data class Conversation(
    val uuid: String,
    val type: String,
    val name: String?,
    val taskUuid: String?,
    val participants: List<User> = emptyList(),
    val lastMessage: LastMessageInfo? = null
)