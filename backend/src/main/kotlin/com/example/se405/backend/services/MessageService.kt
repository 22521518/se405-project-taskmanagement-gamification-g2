package com.example.se405.backend.services

import com.example.se405.backend.database.model.MessageEntity
import com.example.se405.backend.database.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val participantRepository: ConversationParticipantRepository,
    private val userRepository: UserRepository
) {
    fun getMessagesByConversation(conversationId: UUID, userUuid: UUID): List<MessageEntity> {
        val conversation = conversationRepository.findById(conversationId).orElseThrow()
        val user = userRepository.findById(userUuid).orElseThrow()

        // Kiểm tra quyền: Chỉ thành viên trong phòng mới được xem tin nhắn
        participantRepository.findByConversationAndUser(conversation, user)
            .orElseThrow { IllegalAccessException("Bạn không có quyền xem cuộc hội thoại này") }

        return messageRepository.findByConversationUuidOrderByCreatedAtAsc(conversationId)
    }

    private val messageSink = Sinks.many().multicast().onBackpressureBuffer<MessageEntity>()

    @Transactional
    fun sendMessage(conversationId: UUID, senderId: UUID, content: String): MessageEntity {
        val conversation = conversationRepository.findById(conversationId).orElseThrow()
        val sender = userRepository.findById(senderId).orElseThrow()
        participantRepository.findByConversationAndUser(conversation, sender)
            .orElseThrow { IllegalAccessException("Bạn không phải là thành viên của phòng chat này") }

        val message = MessageEntity(
            content = content,
            conversation = conversation,
            sender = sender,
            createdAt = LocalDateTime.now()
        )

        val savedMessage = messageRepository.save(message)
        messageSink.tryEmitNext(savedMessage)
        return savedMessage
    }

    fun subscribeToMessages(conversationId: UUID): Flux<MessageEntity> {
        return messageSink.asFlux()
            .filter { it.conversation.uuid == conversationId } // Chỉ nghe tin của đúng phòng này
    }
}