package com.example.se405.android.features.chat_management.data.repositoryImpl

import com.apollographql.apollo.ApolloClient
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.features.chat_management.domain.entity.Conversation
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.graphql.CreateConversationMutation
import com.example.se405.android.graphql.GetConversationByTaskQuery
import com.example.se405.android.graphql.GetMessagesByConversationQuery
import com.example.se405.android.graphql.GetMyConversationsQuery
import com.example.se405.android.graphql.OnMessageAddedSubscription
import com.example.se405.android.graphql.SendMessageMutation
import com.example.se405.android.graphql.type.ConversationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ChatRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val authPrefs: AuthPreferences // Tên biến được khai báo là authPrefs
) : ChatRepository {

    // 1. Trạm trung chuyển: Lấy Conversation ID từ Task ID
    override suspend fun getConversationByTask(taskId: String): Result<String> {
        return try {
            val response = apolloClient.query(GetConversationByTaskQuery(taskId)).execute()
            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.first()?.message))
            } else {
                val conversationId = response.data?.getConversationByTask?.uuid.toString()
                Result.success(conversationId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Lấy danh sách tin nhắn cũ
    override suspend fun getMessagesByConversation(conversationId: String): Result<List<MessageEntity>> {
        return try {
            // Đã sửa lại thành authPrefs để gọi đúng tham số được tiêm vào
            val myUserId = authPrefs.userId.first() ?: ""

            val response = apolloClient.query(GetMessagesByConversationQuery(conversationId)).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.first()?.message))
            } else {
                val messages = response.data?.getMessagesByConversation?.map { dto ->
                    MessageEntity(
                        uuid = Uuid.parse(dto.uuid.toString()),
                        content = dto.content,
                        conversationId = Uuid.parse(conversationId),
                        sender = User(
                            uuid = Uuid.parse(dto.sender.uuid.toString()),
                            email = "",
                            username = "",
                            displayName = dto.sender.displayName ?: "Người dùng ẩn danh",
                            avatarUrl = dto.sender.avatarUrl ?: "",
                            passwordHash = null,
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now()
                        ),
                        createdAt = parseIsoDate(dto.createdAt.toString()),
                        isOwnMessage = dto.sender.uuid.toString() == myUserId
                    )
                } ?: emptyList()

                Result.success(messages)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Gửi tin nhắn
    override suspend fun sendMessage(conversationId: String, content: String): Result<Unit> {
        return try {
            // Đã sửa tên tham số từ taskId thành conversationId cho đúng logic mới
            val response = apolloClient.mutation(SendMessageMutation(conversationId = conversationId, content = content)).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.first()?.message))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. Lấy danh sách các cuộc hội thoại
    override fun getMyConversations(): Flow<List<Conversation>> = flow {
        val response = apolloClient.query(GetMyConversationsQuery()).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message)
        }

        val conversations = response.data?.getMyConversations?.map { dto ->
            // Đảm bảo kiểu dữ liệu mapping khớp với data class Conversation của bạn
            Conversation(
                uuid = dto.uuid,
                type = dto.type.rawValue,
                name = dto.name,
                taskUuid = dto.taskId
            )
        } ?: emptyList()

        emit(conversations)
    }

    // 5. Tạo phòng chat mới
    override suspend fun createConversation(
        participantIds: List<String>,
        isGroup: Boolean,
        name: String?
    ): Result<String> {
        return try {
            val type = if (isGroup || participantIds.size > 1) ConversationType.GROUP else ConversationType.DIRECT

            val response = apolloClient.mutation(
                CreateConversationMutation(
                    participantIds = participantIds,
                    type = type,
                    name = com.apollographql.apollo.api.Optional.presentIfNotNull(name)
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.first()?.message))
            } else {
                val newConversationId = response.data?.createConversation?.uuid.toString()
                Result.success(newConversationId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. Lắng nghe tin nhắn mới qua WebSocket
    override fun subscribeToMessages(conversationId: String): Flow<MessageEntity> {
        return apolloClient.subscription(OnMessageAddedSubscription(conversationId)).toFlow().mapNotNull { response ->
            val myUserId = authPrefs.userId.first() ?: ""
            val dto = response.data?.messageAdded ?: return@mapNotNull null

            MessageEntity(
                uuid = Uuid.parse(dto.uuid),
                content = dto.content,
                conversationId = Uuid.parse(conversationId),
                sender = User(
                    uuid = Uuid.parse(dto.sender.uuid),
                    email = "",
                    username = "",
                    displayName = dto.sender.displayName ?: "Người dùng ẩn danh",
                    avatarUrl = dto.sender.avatarUrl ?: "",
                    passwordHash = null,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                createdAt = parseIsoDate(dto.createdAt),
                isOwnMessage = dto.sender.uuid == myUserId
            )
        }
    }

    // Hàm phụ trợ xử lý thời gian
    private fun parseIsoDate(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}