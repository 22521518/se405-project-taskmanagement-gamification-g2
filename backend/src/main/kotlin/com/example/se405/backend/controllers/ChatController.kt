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
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

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
    fun getMyConversations(): List<ConversationEntity> {
        return conversationService.getUserConversations(getCurrentUserUuid())
    }

    @QueryMapping
    fun getMessagesByConversation(@Argument conversationId: String): List<MessageEntity> {
        return messageService.getMessagesByConversation(UUID.fromString(conversationId), getCurrentUserUuid())
    }

    @QueryMapping
    fun getConversationByTask(@Argument taskId: String, @Argument taskName: String): ConversationEntity {
        val userUuid = getCurrentUserUuid()
        val taskUuid = UUID.fromString(taskId)

        val conversation = conversationService.getConversationByTaskId(taskUuid)
            ?: conversationService.getOrCreateTaskConversation(taskUuid, taskName)

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
        val myUuid = getCurrentUserUuid()
        val uuids = participantIds.map { UUID.fromString(it) }.toMutableSet()
        uuids.add(myUuid)
        return conversationService.createConversation(type, name, uuids.toList())
    }

    @MutationMapping
    fun sendMessage(
        @Argument conversationId: String,
        @Argument content: String
    ): MessageEntity {
        return messageService.sendMessage(UUID.fromString(conversationId), getCurrentUserUuid(), content)
    }

    @SubscriptionMapping
    fun messageAdded(@Argument conversationId: String): Flux<MessagePayload> {
        println("================ CONTROLLER WEBSOCKET ================")
        println("📥 Đã chạm vào hàm messageAdded tại Controller!")
        println("🆔 Tham số conversationId nhận được từ Android: '$conversationId'")
        if (conversationId.isBlank()) {
            println("❌ Thất bại: conversationId truyền lên bị NULL hoặc RỖNG!")
            println("=====================================================")
            return Flux.empty()
        }
        return try {
            val uuid = UUID.fromString(conversationId.trim())
            println("✅ Parse UUID thành công: $uuid. Tiến hành gọi Service...")
            println("=====================================================")

            messageService.subscribeToMessages(uuid)

        } catch (e: IllegalArgumentException) {
            println("❌ Thất bại: Chuỗi '$conversationId' KHÔNG ĐÚNG định dạng UUID!")
            e.printStackTrace()
            println("=====================================================")
            Flux.empty()
        } catch (e: Exception) {
            println("❌ Lỗi không xác định tại Controller: ${e.message}")
            e.printStackTrace()
            println("=====================================================")
            Flux.empty()
        }
    }

    @MutationMapping
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
}