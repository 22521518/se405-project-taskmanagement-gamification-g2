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
                uuid = Uuid.parse(dto.uuid),
                content = dto.content,
                taskUuid = Uuid.parse(taskId),
                sender = User(
                    uuid = Uuid.parse(dto.sender.uuid),
                    displayName = dto.sender.displayName,
                    // Mock các trường User không cần thiết cho UI chat
                    email = "", username = "", passwordHash = "", avatarUrl = "",
                    createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()
                ),
                createdAt = parseIsoDate(dto.createdAt),
                // 4. Kiểm tra tin nhắn của mình bằng cách so sánh String ID
                isOwnMessage = dto.sender.uuid == myUserId
            )
        } ?: emptyList()

        // 5. Phát (emit) list tin nhắn ra cho ViewModel
        emit(messages)
    }

    override suspend fun sendMessage(taskId: String, content: String): Result<Unit> {
        return try {
            val response = apolloClient.mutation(SendMessageMutation(taskId, content)).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.first()?.message))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hàm hỗ trợ chuyển đổi chuỗi thời gian ISO-8601 từ Server thành LocalDateTime của Android
     */
    private fun parseIsoDate(dateString: String): LocalDateTime {
        return try {
            // Định dạng phổ biến nhất của Backend trả về
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now() // Phòng hờ lỗi crash do format sai
        }
    }
}