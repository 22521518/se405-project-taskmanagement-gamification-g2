package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.ConversationEntity
import com.example.se405.backend.database.model.ConversationType
import com.example.se405.backend.database.model.MessageEntity
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.repository.UserRepository
import com.example.se405.backend.services.ConversationService
import com.example.se405.backend.services.MessageService
import jakarta.transaction.Transactional
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import java.util.UUID
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Controller
class ChatController(
    private val messageService: MessageService,
    private val conversationService: ConversationService,
    private val userRepository: UserRepository
) {
    private fun getCurrentUserUuid(): UUID {
        val auth = SecurityContextHolder.getContext().authentication

        if (auth == null || auth.name == "anonymousUser") {
            throw Exception("Unauthorized: Yêu cầu bị từ chối do thiếu JWT Token hợp lệ!")
        }

        val username = auth.name
        val user = userRepository.findByUsername(username)
            ?: throw Exception("Unauthorized: Không tìm thấy user '$username' trong Database")

        return user.uuid!!
    }

    // ==================== QUERIES ====================

    @QueryMapping
    fun getMyConversations(): List<ConversationEntity> {
        val userUuid = getCurrentUserUuid()
        return conversationService.getUserConversations(userUuid)
    }

    @QueryMapping
    fun getMessagesByConversation(@Argument conversationId: String): List<MessageEntity> {
        val userUuid = getCurrentUserUuid()
        return messageService.getMessagesByConversation(UUID.fromString(conversationId), userUuid)
    }

    @QueryMapping
    fun getMessagesByTask(@Argument taskId: String): List<MessageEntity> {
        val userUuid = getCurrentUserUuid()
        val inputUuid = UUID.fromString(taskId)
        val conversation = conversationService.getConversationById(inputUuid)
            ?: conversationService.getOrCreateTaskConversation(inputUuid)
        conversationService.addParticipantIfNotExists(conversation.uuid!!, userUuid)
        return messageService.getMessagesByConversation(conversation.uuid!!, userUuid)
    }

    @QueryMapping
    fun getAllUsers(): List<UserEntity> {
        return userRepository.findAll()
    }

    // ==================== MUTATIONS ====================

    @MutationMapping
    fun createConversation(
        @Argument type: ConversationType,
        @Argument name: String?,
        @Argument participantIds: List<String>
    ): ConversationEntity {
        val uuids = participantIds.map { UUID.fromString(it) }
        return conversationService.createConversation(type, name, uuids)
    }
    // ==================== SUBSCRIPTIONS ====================

    @SubscriptionMapping
    fun messageAdded(@Argument conversationId: String): Flux<MessageEntity> {
        // Trả về một luồng (Stream) dữ liệu liên tục thay vì 1 List cố định
        return messageService.subscribeToMessages(UUID.fromString(conversationId))
    }
}