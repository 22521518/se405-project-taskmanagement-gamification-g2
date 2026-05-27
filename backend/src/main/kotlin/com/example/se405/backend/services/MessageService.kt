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
    fun sendMessage(conversationId: UUID, senderId: UUID, content: String): MessageEntity {
        val conversation = conversationRepository.findById(conversationId).orElseThrow()
        val sender = userRepository.findById(senderId).orElseThrow()

        participantRepository.findByConversationAndUser(conversation, sender)
            .orElseThrow { IllegalAccessException("Bạn không phải là thành viên của phòng chat này") }

        val savedMessage = messageRepository.save(
            MessageEntity(
                content = content,
                conversation = conversation,
                sender = sender,
                createdAt = LocalDateTime.now()
            )
        )

        val payload = MessagePayload(
            uuid = savedMessage.uuid.toString(),
            content = savedMessage.content,
            createdAt = savedMessage.createdAt.toString(),
            conversationId = conversationId.toString(),
            sender =
                UserPayload(
                uuid = sender.uuid.toString(),
                displayName = sender.displayName,
                avatarUrl = sender.avatarUrl,
                email = sender.email
            )
        )

        getOrCreateSink(conversationId).tryEmitNext(payload)
        return savedMessage
    }

    fun subscribeToMessages(conversationId: UUID): Flux<MessagePayload> {
        println("🚀 WebSocket: Có người yêu cầu Lắng nghe phòng $conversationId")
        return getOrCreateSink(conversationId).asFlux()
            .doOnSubscribe { println("🔗 WebSocket: Một Client VỪA KẾT NỐI thành công vào ống (Sink) của phòng $conversationId") }
            .doOnCancel { println("❌ WebSocket: Client VỪA NGẮT KẾT NỐI khỏi phòng $conversationId") }
            .doOnComplete { println("✅ WebSocket: Ống (Sink) của phòng $conversationId ĐÃ ĐÓNG HOÀN TOÀN") }
            .doOnError { e -> println("🚨 WebSocket: Ống (Sink) BỊ LỖI - ${e.message}") }

    }
}