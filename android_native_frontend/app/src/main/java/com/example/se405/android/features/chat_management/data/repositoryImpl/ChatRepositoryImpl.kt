package com.example.se405.android.features.chat_management.data.repositoryImpl

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.data.CloudinaryResponse
import com.example.se405.android.features.chat_management.domain.entity.Conversation
import com.example.se405.android.features.chat_management.domain.entity.LastMessageInfo
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.chat_management.domain.entity.ReplyMessageInfo
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.graphql.CreateConversationMutation
import com.example.se405.android.graphql.GetConversationByTaskQuery
import com.example.se405.android.graphql.GetMessagesByConversationQuery
import com.example.se405.android.graphql.GetMyConversationsQuery
import com.example.se405.android.graphql.OnMessageAddedSubscription
import com.example.se405.android.graphql.RenameConversationMutation
import com.example.se405.android.graphql.SendMessageMutation
import com.example.se405.android.graphql.type.ConversationType
import com.google.firebase.storage.FirebaseStorage
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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart      // 💡 Thêm 2 dòng import này
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalUuidApi::class)
class ChatRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val authPrefs: AuthPreferences
) : ChatRepository {
    override suspend fun getConversationByTask(taskId: String, taskName: String): Result<String> {
        return try {
            val response = apolloClient.query(GetConversationByTaskQuery(taskId = taskId, taskName = taskName)).execute()

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

    override suspend fun uploadFileToCloudinary(fileBytes: ByteArray, fileName: String, isImage: Boolean): Result<String> = runCatching {
        if (isImage) {
            // ==========================================
            // 1. NHÁNH ẢNH: UPLOAD LÊN CLOUDINARY
            // ==========================================
            val cloudName = "de5l5byyn"
            val resourceType = "image"
            val uploadPreset = "se405_attachment_upload"

            val url = "https://api.cloudinary.com/v1_1/$cloudName/$resourceType/upload"

            val cleanClient = HttpClient(OkHttp) {
                install(Logging) { level = LogLevel.INFO }
            }

            cleanClient.use { client ->
                val response = client.submitFormWithBinaryData(
                    url = url,
                    formData = formData {
                        append("upload_preset", uploadPreset)
                        append("file", fileBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        })
                    }
                )

                response.ensureSuccess()
                val responseBody = response.bodyAsText()
                val jsonParser = Json { ignoreUnknownKeys = true }
                val cloudinaryResponse = jsonParser.decodeFromString<CloudinaryResponse>(responseBody)

                return@runCatching cloudinaryResponse.secure_url
            }
        } else {
            // ==========================================
            // 2. NHÁNH FILE: UPLOAD LÊN FIREBASE STORAGE
            // ==========================================
            val storageRef = FirebaseStorage.getInstance().reference
                .child("chat_documents/${System.currentTimeMillis()}_$fileName")
            storageRef.putBytes(fileBytes).await()

            val downloadUrl = storageRef.downloadUrl.await()

            return@runCatching downloadUrl.toString()
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
            type = dto.type.name, // Lấy Enum từ GraphQL map thành String
            fileUrl = dto.fileUrl,
            fileName = dto.fileName,
            fileSize = dto.fileSize,
            sender = User(
                uuid = Uuid.parse(dto.sender.uuid),
                email = dto.sender.email,
                username = "unknown",
                displayName = dto.sender.displayName ?: "Người dùng ẩn danh",
                avatarUrl = dto.sender.avatarUrl ?: "",
                passwordHash = null,
                createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()
            ),
            createdAt = parseIsoDate(dto.createdAt),
            isOwnMessage = isMine,
            replyTo = dto.replyTo?.let { ReplyMessageInfo(Uuid.parse(it.uuid), it.content, it.sender.displayName ?: "") }
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

    override suspend fun sendMessage(
        conversationId: String,
        content: String,
        replyToId: String?,
        type: String,
        fileUrl: String?,
        fileName: String?,
        fileSize: String?
    ): Result<MessageEntity> {
        return try {
            // Chuyển String Type từ Mobile sang cấu trúc Enum chuẩn mã sinh bởi Apollo
            val gqlType = when(type) {
                "IMAGE" -> com.example.se405.android.graphql.type.MessageType.IMAGE
                "FILE" -> com.example.se405.android.graphql.type.MessageType.FILE
                else -> com.example.se405.android.graphql.type.MessageType.TEXT
            }

            val response = apolloClient.mutation(
                SendMessageMutation(
                    conversationId = conversationId,
                    content = content,
                    replyToId = Optional.presentIfNotNull(replyToId),
                    type = gqlType,
                    fileUrl = Optional.presentIfNotNull(fileUrl),
                    fileName = Optional.presentIfNotNull(fileName),
                    fileSize = Optional.presentIfNotNull(fileSize)
                )
            ).execute()

            if (response.hasErrors()) {
                return Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Lỗi từ Backend"))
            }

            val dto = response.data?.sendMessage ?: return Result.failure(Exception("Không lấy dữ liệu thành công"))

            Result.success(
                MessageEntity(
                    uuid = Uuid.parse(dto.uuid),
                    content = dto.content,
                    conversationId = Uuid.parse(dto.conversationId),
                    type = dto.type.name,
                    fileUrl = dto.fileUrl,
                    fileName = dto.fileName,
                    fileSize = dto.fileSize,
                    sender = User(uuid = Uuid.parse(dto.sender.uuid), email = dto.sender.email, username = "unknown", displayName = dto.sender.displayName ?: "Tôi", avatarUrl = dto.sender.avatarUrl, passwordHash = null, createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()),
                    createdAt = parseIsoDate(dto.createdAt),
                    isOwnMessage = true,
                    replyTo = dto.replyTo?.let { ReplyMessageInfo(Uuid.parse(it.uuid), it.content, it.sender.displayName ?: "") }
                )
            )
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
            val lastMsg = dto.lastMessage?.let { msgDto ->
                LastMessageInfo(
                    content = msgDto.content,
                    createdAt = parseIsoDate(msgDto.createdAt),
                    senderId = msgDto.sender.uuid,
                    senderName = msgDto.sender.displayName ?: "Người dùng"
                )
            }
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
                },lastMessage = lastMsg
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

    override fun subscribeToMessages(conversationId: String, myUserId: String): Flow<MessageEntity> {
        return apolloClient.subscription(OnMessageAddedSubscription(conversationId))
            .toFlow()
            .mapNotNull { response ->
                if (response.exception != null) return@mapNotNull null
                if (response.hasErrors()) return@mapNotNull null

                val dto = response.data?.messageAdded ?: return@mapNotNull null

                try {
                    val isMine = dto.sender.uuid == myUserId

                    val replyInfo = dto.replyTo?.let { replyDto ->
                        ReplyMessageInfo(
                            uuid = Uuid.parse(replyDto.uuid),
                            content = replyDto.content,
                            senderName = replyDto.sender.displayName ?: "Người dùng"
                        )
                    }

                    MessageEntity(
                        uuid = Uuid.parse(dto.uuid),
                        content = dto.content,
                        conversationId = Uuid.parse(conversationId),
                        sender = User(
                            uuid = Uuid.parse(dto.sender.uuid),
                            email = dto.sender.email,
                            username = "unknown",
                            displayName = dto.sender.displayName ?: "Người dùng",
                            avatarUrl = dto.sender.avatarUrl,
                            passwordHash = null,
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now()
                        ),
                        type = dto.type.name,
                        fileUrl = dto.fileUrl,
                        fileName = dto.fileName,
                        fileSize = dto.fileSize,
                        createdAt = parseIsoDate(dto.createdAt),
                        isOwnMessage = isMine,
                        replyTo = replyInfo
                    )
                } catch (e: Exception) {
                    null
                }
            }
    }

    override suspend fun renameConversation(conversationId: String, newName: String): Result<Boolean> {
        return try {
            val response = apolloClient.mutation(
                RenameConversationMutation(
                    conversationId,
                    newName
                )
            ).execute()
            if (response.hasErrors()) Result.failure(Exception(response.errors?.first()?.message))
            else Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
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