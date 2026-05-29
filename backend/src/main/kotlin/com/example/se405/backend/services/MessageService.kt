package com.example.se405.backend.services

import com.example.se405.backend.controllers.dtos.MessagePayload
import com.example.se405.backend.controllers.dtos.UserPayload
import com.example.se405.backend.database.model.MessageEntity
import com.example.se405.backend.database.repository.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val participantRepository: ConversationParticipantRepository,
    private val userRepository: UserRepository
) {
    // CHAT SINKS MANAGEMENT
    private val conversationSinks = ConcurrentHashMap<UUID, Sinks.Many<MessagePayload>>()

    private fun getOrCreateSink(conversationId: UUID): Sinks.Many<MessagePayload> {
        return conversationSinks.computeIfAbsent(conversationId) {
            Sinks.many().multicast().onBackpressureBuffer(256, false)
        }
    }

    // BUSINESS LOGIC
    fun getMessagesByConversation(conversationId: UUID, userUuid: UUID): List<MessageEntity> {
        val conversation = conversationRepository.findById(conversationId).orElseThrow()
        val user = userRepository.findById(userUuid).orElseThrow()

        participantRepository.findByConversationAndUser(conversation, user)
            .orElseThrow { IllegalAccessException("Bạn không có quyền xem cuộc hội thoại này") }

        return messageRepository.findByConversationUuidOrderByCreatedAtAsc(conversationId)
    }

    @Transactional
    fun sendMessage(conversationId: UUID, senderId: UUID, content: String, replyToId: UUID? = null, type: String = "TEXT",fileUrl: String? = null, fileName: String? = null, fileSize: String? = null): MessageEntity {
        val conversation = conversationRepository.findById(conversationId).orElseThrow()
        val sender = userRepository.findById(senderId).orElseThrow()

        participantRepository.findByConversationAndUser(conversation, sender)
            .orElseThrow { IllegalAccessException("Bạn không phải là thành viên của phòng chat này") }

        val replyToMessage = replyToId?.let {
            messageRepository.findById(it).orElse(null)
        }

        val savedMessage = messageRepository.save(
            MessageEntity(
                content = content,
                type = type,
                fileUrl = fileUrl,
                fileName = fileName,
                fileSize = fileSize,
                conversation = conversation,
                sender = sender,
                replyTo = replyToMessage,
                createdAt = LocalDateTime.now()
            )
        )

        fun mapToPayload(entity: MessageEntity): MessagePayload {
            return MessagePayload(
                uuid = entity.uuid.toString(),
                content = entity.content,
                type = entity.type,
                fileUrl = entity.fileUrl,
                fileName = entity.fileName,
                fileSize = entity.fileSize,
                createdAt = entity.createdAt.toString(),
                conversationId = entity.conversation.uuid.toString(),
                sender = UserPayload(
                    uuid = entity.sender.uuid.toString(),
                    displayName = entity.sender.displayName,
                    avatarUrl = entity.sender.avatarUrl,
                    email = entity.sender.email
                ),
                replyTo = entity.replyTo?.let { mapToPayload(it) } // Đệ quy map tin nhắn gốc
            )
        }

        val payload = mapToPayload(savedMessage)

        // 1. Đẩy tin nhắn qua luồng WebSocket (Real-time)
        getOrCreateSink(conversationId).tryEmitNext(payload)

        // 2. Bắn Push Notification qua Firebase cho các thành viên khác
        val otherParticipants = participantRepository.findByConversation(conversation)
            .filter { it.user.uuid != senderId }

        val notificationBody = when (type) {
            "IMAGE" -> "[Hình ảnh]"
            "FILE" -> "[Tài liệu] $fileName"
            else -> content
        }

        for (participant in otherParticipants) {
            val token = participant.user.fcmToken
            if (!token.isNullOrBlank()) {
                try {
                    val fcmMessage = Message.builder()
                        .setToken(token)
                        .putData("conversationId", conversationId.toString())
                        .putData("senderId", senderId.toString())
                        .putData("type", "CHAT_MESSAGE")
                        .setNotification(
                            Notification.builder()
                                .setTitle(sender.displayName ?: "Tin nhắn mới")
                                .setBody(notificationBody)
                                .build()
                        )
                        .build()

                    FirebaseMessaging.getInstance().send(fcmMessage)
                } catch (e: Exception) {
                    println("🚨 Lỗi khi gửi FCM cho user ${participant.user.username}: ${e.message}")
                }
            }
        }

        return savedMessage
    }

    fun subscribeToMessages(conversationId: UUID): Flux<MessagePayload> {
        return getOrCreateSink(conversationId).asFlux()
    }
}