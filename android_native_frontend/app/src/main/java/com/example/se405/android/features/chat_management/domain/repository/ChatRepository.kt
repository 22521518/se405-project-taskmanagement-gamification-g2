package com.example.se405.android.features.chat_management.domain.repository

import com.example.se405.android.features.chat_management.domain.entity.Conversation
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    // 1. Trạm trung chuyển: Lấy Conversation ID dựa trên Task ID
    suspend fun getConversationByTask(taskId: String): Result<String>

    // 2. Lấy danh sách tin nhắn cũ dựa trên Conversation ID
    suspend fun getMessagesByConversation(conversationId: String): Result<List<MessageEntity>>

    // 3. Gửi tin nhắn (Đã sửa tham số từ taskId -> conversationId)
    suspend fun sendMessage(conversationId: String, content: String): Result<Unit>

    // 4. Lấy danh sách các phòng chat cho màn hình Inbox
    fun getMyConversations(): Flow<List<Conversation>>

    // 5. Tạo phòng chat mới (Đơn hoặc Nhóm)
    suspend fun createConversation(participantIds: List<String>, isGroup: Boolean, name: String? = null): Result<String>

    // (Tùy chọn) 6. Lắng nghe tin nhắn mới qua WebSocket theo thời gian thực
    suspend fun subscribeToMessages(conversationId: String): Flow<MessageEntity>

    suspend fun uploadImageToCloudinary(imageBytes: ByteArray, isAvatar: Boolean = false): Result<String>
}