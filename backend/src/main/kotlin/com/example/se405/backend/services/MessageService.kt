package com.example.se405.backend.services

import com.example.se405.backend.database.model.MessageEntity
import com.example.se405.backend.database.repository.MessageRepository
import com.example.se405.backend.database.repository.TaskRepository
import com.example.se405.backend.database.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {
    fun getMessagesForTask(taskId: UUID): List<MessageEntity> {
        return messageRepository.findByTaskUuidOrderByCreatedAtAsc(taskId)
    }

    @Transactional
    fun sendMessage(taskId: UUID, content: String, senderId: UUID): MessageEntity {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Task not found with id: $taskId") }

        val sender = userRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("User not found with id: $senderId") }

        val message = MessageEntity(
            content = content,
            task = task,
            sender = sender,
            createdAt = LocalDateTime.now()
        )

        return messageRepository.save(message)
    }
}