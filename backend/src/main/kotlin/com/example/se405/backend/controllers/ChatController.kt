package com.example.se405.backend.controllers

import com.example.se405.backend.controllers.dtos.MessagePayload
import com.example.se405.backend.database.model.ConversationEntity
import com.example.se405.backend.database.model.ConversationType
import com.example.se405.backend.database.model.MessageEntity
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.repository.TaskRepository
import com.example.se405.backend.database.repository.UserRepository
import com.example.se405.backend.services.ConversationService
import com.example.se405.backend.services.MessageService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import java.util.UUID
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux

@Controller
class ChatController(
    private val messageService: MessageService,
    private val conversationService: ConversationService,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
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
    @Transactional(readOnly = true)
    fun getMyConversations(): List<ConversationEntity> {
        return conversationService.getUserConversations(getCurrentUserUuid())
    }

    @QueryMapping
    @Transactional(readOnly = true)
    fun getMessagesByConversation(@Argument conversationId: String): List<MessageEntity> {
        return messageService.getMessagesByConversation(UUID.fromString(conversationId), getCurrentUserUuid())
    }

    @QueryMapping
    @Transactional
    fun getConversationByTask(@Argument taskId: String, @Argument taskName: String): ConversationEntity {
        val userUuid = getCurrentUserUuid()
        val taskUuid = UUID.fromString(taskId)

        val conversation = conversationService.getConversationByTaskId(taskUuid)
            ?: conversationService.getOrCreateTaskConversation(taskUuid, taskName)

        conversationService.addParticipantIfNotExists(conversation.uuid!!, userUuid)
        return conversation
    }

    @QueryMapping
    @Transactional
    fun getConversationByWorkspace(@Argument workspaceId: String, @Argument name: String): ConversationEntity {
        val userUuid = getCurrentUserUuid()
        val conversation = conversationService.getOrCreateWorkspaceConversation(UUID.fromString(workspaceId), name)
        conversationService.addParticipantIfNotExists(conversation.uuid!!, userUuid)
        return conversation
    }

    @QueryMapping
    @Transactional
    fun getConversationByProject(@Argument projectId: String, @Argument name: String): ConversationEntity {
        val userUuid = getCurrentUserUuid()
        val conversation = conversationService.getOrCreateProjectConversation(UUID.fromString(projectId), name)
        conversationService.addParticipantIfNotExists(conversation.uuid!!, userUuid)
        return conversation
    }

    @QueryMapping
    @Transactional(readOnly = true)
    fun getPinnedMessages(@Argument conversationId: String): List<MessageEntity> {
        return messageService.getPinnedMessages(UUID.fromString(conversationId))
    }

    @QueryMapping
    @Transactional(readOnly = true)
    fun getSharedMedia(@Argument conversationId: String): List<MessageEntity> {
        return messageService.getSharedMedia(UUID.fromString(conversationId))
    }

    @MutationMapping
    @Transactional
    fun togglePinMessage(@Argument messageId: String): MessageEntity {
        return messageService.togglePinMessage(UUID.fromString(messageId))
    }

    @MutationMapping
    @Transactional
    fun revokeMessage(@Argument messageId: String): MessageEntity {
        return messageService.revokeMessage(UUID.fromString(messageId), getCurrentUserUuid())
    }

    @QueryMapping
    fun getAllUsers(): List<UserEntity> {
        return userRepository.findAll()
    }

    @MutationMapping
    @Transactional
    fun createConversation(
        @Argument type: ConversationType,
        @Argument name: String?,
        @Argument participantIds: List<String>
    ): ConversationEntity {
        val myUuid = getCurrentUserUuid()
        val uuids = participantIds.map { UUID.fromString(it) }.toMutableSet()
        uuids.add(myUuid)
        return conversationService.createConversation(type, name, uuids.toList())
    }

    @MutationMapping
    @Transactional
    fun sendMessage(
        @Argument conversationId: String,
        @Argument content: String,
        @Argument replyToId: String?,
        @Argument type: String,
        @Argument fileUrl: String?,
        @Argument fileName: String?,
        @Argument fileSize: String?,
        @Argument mentionedUserIds: List<String>?
    ): MessageEntity {
        val principal = SecurityContextHolder.getContext().authentication.principal
        val senderUuid = principal as? UUID ?: getCurrentUserUuid()
        val convUuid = UUID.fromString(conversationId)
        val replyToUuid = if (!replyToId.isNullOrBlank()) UUID.fromString(replyToId) else null

        // 💡 XỬ LÝ MENTIONS: Map an toàn từ List<String> sang List<UUID>
        val mentionedUuids = mentionedUserIds?.mapNotNull { idStr ->
            try {
                UUID.fromString(idStr)
            } catch (e: Exception) {
                null // Bỏ qua nếu có ID rác/không hợp lệ gửi lên
            }
        }

        return messageService.sendMessage(
            conversationId = convUuid,
            senderId = senderUuid,
            content = content,
            replyToId = replyToUuid,
            type = type,
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize,
            mentionedUserIds = mentionedUuids
        )
    }

    @SubscriptionMapping
    fun messageEvents(@Argument conversationId: String): Flux<MessagePayload> {
        if (conversationId.isBlank()) return Flux.empty()
        return try {
            val uuid = UUID.fromString(conversationId.trim())
            messageService.subscribeToMessages(uuid)
        } catch (e: Exception) {
            Flux.empty()
        }
    }

    @MutationMapping
    @Transactional
    fun renameConversation(
        @Argument conversationId: UUID,
        @Argument newName: String
    ): ConversationEntity {
        return conversationService.renameConversation(conversationId, newName)
    }

    @SchemaMapping(typeName = "Conversation", field = "name")
    fun getDynamicConversationName(conversation: ConversationEntity): String? {
        if (conversation.type == ConversationType.TASK && conversation.taskUuid != null) {
            val task = taskRepository.findById(conversation.taskUuid).orElse(null)
            return task?.title ?: "Thảo luận công việc"
        }
        return conversation.name
    }

    @QueryMapping
    @Transactional(readOnly = true)
    fun getSharedLinks(@Argument conversationId: String): List<MessageEntity> {
        return messageService.getSharedLinks(UUID.fromString(conversationId))
    }

    @QueryMapping
    @Transactional(readOnly = true)
    fun searchMessages(@Argument conversationId: String, @Argument keyword: String): List<MessageEntity> {
        return messageService.searchMessages(UUID.fromString(conversationId), keyword.trim())
    }

    @QueryMapping
    @Transactional(readOnly = true)
    fun getConversationMembers(@Argument conversationId: String): List<UserEntity> {
        return messageService.getConversationMembers(UUID.fromString(conversationId))
    }
}