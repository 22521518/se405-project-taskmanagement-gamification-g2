package com.example.se405.android.features.chat_management.domain.repository

import androidx.core.app.NotificationCompat
import com.example.se405.android.features.chat_management.domain.entity.Conversation
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.users_management.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    // 1. Trạm trung chuyển: Lấy Conversation ID dựa trên Task ID
    suspend fun getConversationByTask(taskId: String, taskName: String): Result<String>

    // 2. Lấy danh sách tin nhắn cũ dựa trên Conversation ID
    suspend fun getMessagesByConversation(conversationId: String): Result<List<MessageEntity>>

    // 3. Gửi tin nhắn (Đã sửa tham số từ taskId -> conversationId)
    suspend fun sendMessage(conversationId: String, content: String, replyToId: String? = null, type: String = "TEXT", fileUrl: String? = null, fileName: String? = null, fileSize: String? = null, mentionedUserIds: List<String>? = null): Result<MessageEntity>

    // 4. Lấy danh sách các phòng chat cho màn hình Inbox
    fun getMyConversations(): Flow<List<Conversation>>

    // 5. Tạo phòng chat mới (Đơn hoặc Nhóm)
    suspend fun createConversation(participantIds: List<String>, isGroup: Boolean, name: String? = null): Result<String>

    fun subscribeToMessages(conversationId: String, myUserId: String): Flow<Pair<String, MessageEntity>>

    suspend fun uploadFileToCloudinary(fileBytes: ByteArray, fileName: String, isImage: Boolean): Result<String>

    suspend fun renameConversation(conversationId: String, newName: String): Result<Boolean>

    suspend fun togglePinMessage(messageId: String): Result<Boolean>
    suspend fun revokeMessage(messageId: String): Result<Boolean>
    suspend fun getPinnedMessages(conversationId: String, myUserId: String): Result<List<MessageEntity>>
    suspend fun getSharedMedia(conversationId: String, myUserId: String): Result<List<MessageEntity>>

    suspend fun getSharedLinks(conversationId: String, myUserId: String): Result<List<MessageEntity>>
    suspend fun searchMessages(conversationId: String, keyword: String, myUserId: String): Result<List<MessageEntity>>
    suspend fun getConversationMembers(conversationId: String): Result<List<User>>
}