package com.example.se405.android.features.chat_management.domain.repository

import com.example.se405.android.features.chat_management.domain.entity.Conversation
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessagesByTask(taskId: String): Flow<List<MessageEntity>>
    suspend fun sendMessage(taskId: String, content: String): Result<Unit>
    fun getMyConversations(): Flow<List<Conversation>>
}