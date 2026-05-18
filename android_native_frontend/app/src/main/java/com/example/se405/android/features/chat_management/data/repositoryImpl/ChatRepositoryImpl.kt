package com.example.se405.android.features.chat_management.data.repositoryImpl

import com.apollographql.apollo.ApolloClient
import com.example.se405.android.graphql.GetMessagesByTaskQuery
import com.example.se405.android.graphql.SendMessageMutation
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import com.example.se405.android.features.users_management.domain.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.example.se405.android.graphql.GetMyConversationsQuery
import com.example.se405.android.features.chat_management.domain.entity.Conversation

@OptIn(ExperimentalUuidApi::class)
class ChatRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val authPrefs: AuthPreferences
) : ChatRepository {

    override fun getMessagesByTask(taskId: String): Flow<List<MessageEntity>> = flow {
        // 1. Lấy User ID dưới dạng String từ AuthPreferences
        val myUserId = authPrefs.userId.first() ?: ""

        // 2. Gọi Apollo Client lấy data
        val response = apolloClient.query(GetMessagesByTaskQuery(taskId)).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message)
        }

        // 3. Map DTO (GraphQL) sang Entity (Giao diện)
        val messages = response.data?.getMessagesByTask?.map { dto ->
            MessageEntity(
                uuid = Uuid.parse(dto.uuid.toString()),
                content = dto.content,
                taskUuid = Uuid.parse(taskId),
                sender = User(
                    uuid = Uuid.parse(dto.sender.uuid.toString()),
                    // Câu query tin nhắn thường không lấy email/username, ta truyền chuỗi rỗng để thỏa mãn Data Class
                    email = "",
                    username = "",
                    displayName = dto.sender.displayName ?: "Người dùng ẩn danh",
                    avatarUrl = ""
                ),
                createdAt = parseIsoDate(dto.createdAt.toString()),
                // 4. Kiểm tra tin nhắn của mình bằng cách so sánh String ID
                isOwnMessage = dto.sender.uuid.toString() == myUserId
            )
        } ?: emptyList()

        // 5. Phát (emit) list tin nhắn ra cho ViewModel
        emit(messages)
    }

    override suspend fun sendMessage(taskId: String, content: String): Result<Unit> {
        return try {
            val response = apolloClient.mutation(SendMessageMutation(conversationId = taskId, content = content)).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.first()?.message))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMyConversations(): Flow<List<Conversation>> = flow {
        val response = apolloClient.query(GetMyConversationsQuery()).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message)
        }

        val conversations = response.data?.getMyConversations?.map { dto ->
            Conversation(
                uuid = dto.uuid.toString(),
                type = dto.type.rawValue,
                name = dto.name,
                taskUuid = dto.taskId?.toString()
            )
        } ?: emptyList()

        emit(conversations)
    }

    private fun parseIsoDate(dateString: String): LocalDateTime {
        return try {
            // Định dạng phổ biến nhất của Backend trả về
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now() // Phòng hờ lỗi crash do format sai
        }
    }
}