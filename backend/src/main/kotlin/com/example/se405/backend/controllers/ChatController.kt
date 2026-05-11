package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.MessageEntity
import com.example.se405.backend.database.repository.UserRepository
import com.example.se405.backend.services.MessageService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class ChatController(
    private val messageService: MessageService,
    private val userRepository: UserRepository // Dùng để tra cứu user từ Token
) {

    @QueryMapping
    fun getMessagesByTask(@Argument taskId: String): List<MessageEntity> {
        return messageService.getMessagesForTask(UUID.fromString(taskId))
    }

    @MutationMapping
    fun sendMessage(@Argument taskId: String, @Argument content: String): MessageEntity {
        // 1. Trích xuất thông tin user từ JWT Token đã được filter ở JwtAuthenticationFilter
        val authentication = SecurityContextHolder.getContext().authentication
        val usernameOrEmail = authentication.name

        // 2. Tìm UUID của User hiện tại (Giả định bạn có hàm findByEmail hoặc findByUsername trong UserRepository)
        // Lưu ý: Nếu JWT của team bạn lưu trực tiếp UUID vào authentication.name, bạn có thể ép kiểu luôn thành UUID mà không cần gọi DB.
        //val sender = userRepository.findByEmail(usernameOrEmail)
        val sender = userRepository.findByUsername(usernameOrEmail)
            ?: throw Exception("Unauthorized: Cannot find user in database")

        // 3. Gọi Service để lưu tin nhắn
        return messageService.sendMessage(
            taskId = UUID.fromString(taskId),
            content = content,
            senderId = sender.uuid!!
        )
    }
}