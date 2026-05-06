package com.example.se405.android.features.chat_management.domain.entity

import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class MessageEntity(
    val uuid: Uuid,
    val content: String,
    val taskUuid: Uuid,
    val sender: User, // Liên kết trực tiếp với Entity User của team
    val createdAt: LocalDateTime,
    val isOwnMessage: Boolean // Thuộc tính FE tự tính toán để hiển thị UI (Trái/Phải)
) {
    init {
        require(content.isNotBlank()) { "Message content must not be blank" }
    }
}