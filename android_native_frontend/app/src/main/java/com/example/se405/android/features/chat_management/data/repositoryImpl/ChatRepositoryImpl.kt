package com.example.se405.android.features.chat_management.data.repositoryImpl

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.data.CloudinaryResponse
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
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ChatRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val authPrefs: AuthPreferences
) : ChatRepository {
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

    override suspend fun getMessagesByConversation(conversationId: String): Result<List<MessageEntity>> {
        return try {
            val myUserId = authPrefs.userId.first() ?: ""

            val response = apolloClient.query(GetMessagesByConversationQuery(conversationId)).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.first()?.message))
            } else {
                val messages = response.data?.getMessagesByConversation?.map { dto ->
                    mapGqlMessageToEntity(dto, myUserId, conversationId)
                } ?: emptyList()

                Result.success(messages)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadImageToCloudinary(imageBytes: ByteArray, isAvatar: Boolean): Result<String> = runCatching {
        val cloudName = "de5l5byyn"
        val uploadPreset = if (isAvatar) "se405_avatar_upload" else "se405_attachment_upload"

        val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

        val cleanClient = HttpClient(OkHttp) {
            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("CLOUDINARY_LOG", message)
                    }
                }
            }
        }

        cleanClient.use { client ->
            val response = client.submitFormWithBinaryData(
                url = url,
                formData = formData {
                    append("upload_preset", uploadPreset)

                    append("file", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                    })
                }
            )

            response.ensureSuccess()

            val responseBody = response.bodyAsText()

            val jsonParser = Json { ignoreUnknownKeys = true }
            val cloudinaryResponse = jsonParser.decodeFromString<CloudinaryResponse>(responseBody)

            cloudinaryResponse.secure_url
        }
    }

    private fun mapGqlMessageToEntity(
        dto: GetMessagesByConversationQuery.GetMessagesByConversation,
        myUserId: String,
        conversationId: String
    ): MessageEntity {

        val isMine = dto.sender.uuid == myUserId

        return MessageEntity(
            uuid = Uuid.parse(dto.uuid),
            content = dto.content,
            conversationId = Uuid.parse(conversationId),
            sender = User(
                uuid = Uuid.parse(dto.sender.uuid),
                email = dto.sender.email,
                username = "unknown",
                displayName = dto.sender.displayName ?: "Người dùng ẩn danh",
                avatarUrl = dto.sender.avatarUrl ?: "",
                passwordHash = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            createdAt = parseIsoDate(dto.createdAt),
            isOwnMessage = isMine
        )
    }

    private suspend fun HttpResponse.ensureSuccess() {
        if (status.value >= 300) {
            val errorBody = try {
                bodyAsText()
            } catch (_: Exception) {
                "No response body"
            }
            throw Exception("HTTP ${status.value}: $errorBody")
        }
    }

    override suspend fun sendMessage(conversationId: String, content: String): Result<Unit> {
        return try {
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

    override fun getMyConversations(): Flow<List<Conversation>> = flow {
        val response = apolloClient.query(GetMyConversationsQuery()).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message)
        }

        val conversations = response.data?.getMyConversations?.map { dto ->
            Conversation(
                uuid = dto.uuid,
                type = dto.type.name,
                name = dto.name,
                taskUuid = dto.taskUuid,
                participants = dto.participants.map { p ->
                    User(
                        uuid = Uuid.parse(p.uuid),
                        displayName = p.displayName ?: "Người dùng",
                        avatarUrl = p.avatarUrl,
                        email = p.email,
                        username = p.username,
                        passwordHash = null,
                        createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()
                    )
                },
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
                    name = Optional.presentIfNotNull(name)
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

    override suspend fun subscribeToMessages(conversationId: String): Flow<MessageEntity> {
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

    private fun parseIsoDate(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}