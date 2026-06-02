package com.example.se405.backend.services

import com.example.se405.backend.controllers.dtos.MessagePayload
import com.example.se405.backend.controllers.dtos.UserPayload
import com.example.se405.backend.database.model.MessageEntity
import com.example.se405.backend.database.model.UserEntity
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
    private val userRepository: UserRepository,
    private val cloudinary: com.cloudinary.Cloudinary
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
    fun sendMessage(
        conversationId: UUID,
        senderId: UUID,
        content: String,
        replyToId: UUID? = null,
        type: String = "TEXT",
        fileUrl: String? = null,
        fileName: String? = null,
        fileSize: String? = null,
        mentionedUserIds: List<UUID>? = null
    ): MessageEntity {
        val conversation = conversationRepository.findById(conversationId).orElseThrow()
        val sender = userRepository.findById(senderId).orElseThrow()

        participantRepository.findByConversationAndUser(conversation, sender)
            .orElseThrow { IllegalAccessException("Bạn không phải là thành viên của phòng chat này") }

        val replyToMessage = replyToId?.let {
            messageRepository.findById(it).orElse(null)
        }

        // 💡 Lấy danh sách User Entity dựa trên các ID được truyền lên
        val mentionedUsers = if (!mentionedUserIds.isNullOrEmpty()) {
            userRepository.findAllById(mentionedUserIds).toMutableList()
        } else {
            mutableListOf()
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
                mentions = mentionedUsers,
                createdAt = LocalDateTime.now()
            )
        )

        val payload = MessagePayload("CREATED", savedMessage)

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
                    val isMentioned = mentionedUsers.any { it.uuid == participant.user.uuid }
                    val notifTitle = if (isMentioned) {
                        "${sender.displayName} đã nhắc đến bạn"
                    } else {
                        // Nếu chat nhóm thì có thể để "Tên Nhóm", ở đây tạm dùng tên người gửi
                        sender.displayName ?: "Tin nhắn mới"
                    }
                    val notifBody = if (isMentioned) {
                        content // Hiện nguyên văn câu chat để họ thấy mình bị réo tên vì chuyện gì
                    } else {
                        notificationBody // Text bình thường hoặc [Hình ảnh], [Tài liệu]
                    }
                    val fcmMessage = Message.builder()
                        .setToken(token)
                        .putData("conversationId", conversationId.toString())
                        .putData("senderId", senderId.toString())
                        .putData("type", if (isMentioned) "MENTION" else "CHAT_MESSAGE")
                        .setNotification(
                            Notification.builder()
                                .setTitle(sender.displayName ?: "Tin nhắn mới")
                                .setBody(notificationBody)
                                .build()
                        )
                        .build()

                    FirebaseMessaging.getInstance().send(fcmMessage)
                } catch (e: Exception) { }
            }
        }

        return savedMessage
    }

    @Transactional
    fun togglePinMessage(messageId: UUID): MessageEntity {
        val msg = messageRepository.findById(messageId).orElseThrow { Exception("Không tìm thấy tin nhắn") }
        val conversationId = msg.conversation.uuid!!

        if (msg.isPinned) {
            msg.isPinned = false
            msg.pinnedAt = null
            messageRepository.save(msg)
            getOrCreateSink(conversationId).tryEmitNext(MessagePayload("UNPINNED", msg))
        } else {
            val currentPinned = messageRepository.findByConversationUuidAndIsPinnedTrueOrderByPinnedAtAsc(conversationId)

            if (currentPinned.size >= 3) {
                val oldestPinned = currentPinned[0]
                oldestPinned.isPinned = false
                oldestPinned.pinnedAt = null
                messageRepository.save(oldestPinned)
                getOrCreateSink(conversationId).tryEmitNext(MessagePayload("UNPINNED", oldestPinned))
            }

            msg.isPinned = true
            msg.pinnedAt = LocalDateTime.now()
            messageRepository.save(msg)
            getOrCreateSink(conversationId).tryEmitNext(MessagePayload("PINNED", msg))
        }
        return msg
    }

    @Transactional
    fun revokeMessage(messageId: UUID, currentUserId: UUID): MessageEntity {
        val msg = messageRepository.findById(messageId).orElseThrow { Exception("Không tìm thấy tin nhắn") }

        if (msg.sender.uuid != currentUserId) {
            throw Exception("Unauthorized: Bạn không có quyền thu hồi tin nhắn của người khác")
        }

        msg.isRevoked = true

        if (msg.isPinned) {
            msg.isPinned = false
            msg.pinnedAt = null
        }

        try {
            if (msg.type == "FILE" && !msg.fileName.isNullOrBlank()) {
                val bucketName = "se405-d156d.firebasestorage.app"
                val bucket = com.google.firebase.cloud.StorageClient.getInstance().bucket(bucketName)

                val blob = bucket.get("chat_documents/${msg.fileName}")
                if (blob != null) {
                    blob.delete()
                }

            } else if (msg.type == "IMAGE" && !msg.fileUrl.isNullOrBlank()) {
                val publicId = msg.fileUrl!!.substringAfterLast("/").substringBeforeLast(".")
                cloudinary.uploader().destroy(publicId, com.cloudinary.utils.ObjectUtils.emptyMap())
            }
        } catch (e: Exception) { }

        msg.content = ""
        msg.fileUrl = null
        msg.fileName = null
        msg.fileSize = null

        // 💡 Mở rộng Scrubbing: Nếu xóa tin nhắn thì xóa luôn cả danh sách tag để dọn dẹp data
        msg.mentions.clear()

        messageRepository.save(msg)
        getOrCreateSink(msg.conversation.uuid!!).tryEmitNext(MessagePayload("REVOKED", msg))
        return msg
    }

    fun getPinnedMessages(conversationId: UUID): List<MessageEntity> {
        return messageRepository.findByConversationUuidAndIsPinnedTrueOrderByPinnedAtAsc(conversationId)
    }

    fun getSharedMedia(conversationId: UUID): List<MessageEntity> {
        return messageRepository.findByConversationUuidAndTypeInAndIsRevokedFalseOrderByCreatedAtDesc(
            conversationId,
            listOf("IMAGE", "FILE")
        )
    }

    fun subscribeToMessages(conversationId: UUID): Flux<MessagePayload> {
        return getOrCreateSink(conversationId).asFlux()
    }

    fun getSharedLinks(conversationId: UUID): List<MessageEntity> {
        return messageRepository.findByConversationUuidAndTypeAndContentContainingIgnoreCaseAndIsRevokedFalseOrderByCreatedAtDesc(
            conversationId, "TEXT", "http"
        )
    }

    fun searchMessages(conversationId: UUID, keyword: String): List<MessageEntity> {
        return messageRepository.findByConversationUuidAndContentContainingIgnoreCaseAndIsRevokedFalseOrderByCreatedAtDesc(
            conversationId, keyword
        )
    }

    fun getConversationMembers(conversationId: UUID): List<UserEntity> {
        val conversation = conversationRepository.findById(conversationId)
            .orElseThrow { Exception("Không tìm thấy phòng chat") }

        return participantRepository.findByConversation(conversation).map { participant ->
            participant.user
        }
    }
}