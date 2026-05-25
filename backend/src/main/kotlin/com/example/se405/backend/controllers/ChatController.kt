package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.ConversationEntity
import com.example.se405.backend.database.model.ConversationType
import com.example.se405.backend.database.model.MessageEntity
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.repository.UserRepository
import com.example.se405.backend.services.ConversationService
import com.example.se405.backend.services.MessageService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import java.util.UUID
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import reactor.core.publisher.Flux

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
        val user = userRepository.findByUsername(auth.name)
            ?: throw Exception("Unauthorized: Không tìm thấy user trong Database")
        return user.uuid!!
    }

    @QueryMapping
    fun getMyConversations(): List<ConversationEntity> {
        return conversationService.getUserConversations(getCurrentUserUuid())
    }

    @QueryMapping
    fun getMessagesByConversation(@Argument conversationId: String): List<MessageEntity> {
        return messageService.getMessagesByConversation(UUID.fromString(conversationId), getCurrentUserUuid())
    }

    @QueryMapping
    fun getConversationByTask(@Argument taskId: String): ConversationEntity {
        val userUuid = getCurrentUserUuid()
        val taskUuid = UUID.fromString(taskId)

        val conversation = conversationService.getConversationByTaskId(taskUuid)
            ?: conversationService.getOrCreateTaskConversation(taskUuid)

        conversationService.addParticipantIfNotExists(conversation.uuid!!, userUuid)
        return conversation
    }

    @QueryMapping
    fun getAllUsers(): List<UserEntity> {
        return userRepository.findAll()
    }

    @MutationMapping
    fun createConversation(
        @Argument type: ConversationType,
        @Argument name: String?,
        @Argument participantIds: List<String>
    ): ConversationEntity {
        val uuids = participantIds.map { UUID.fromString(it) }
        return conversationService.createConversation(type, name, uuids)
    }

    @MutationMapping
    fun sendMessage(
        @Argument conversationId: String,
        @Argument content: String
    ): MessageEntity {
        return messageService.sendMessage(UUID.fromString(conversationId), getCurrentUserUuid(), content)
    }

    @SubscriptionMapping
    fun messageAdded(@Argument conversationId: String): Flux<MessageEntity> {
        return messageService.subscribeToMessages(UUID.fromString(conversationId))
    }
}