package com.example.se405.backend.services

import com.example.se405.backend.database.model.*
import com.example.se405.backend.database.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ConversationService(
    private val conversationRepository: ConversationRepository,
    private val participantRepository: ConversationParticipantRepository,
    private val userRepository: UserRepository
) {

    // Lấy tất cả phòng chat của một User để hiện màn hình Inbox
    fun getUserConversations(userUuid: UUID): List<ConversationEntity> {
        val user = userRepository.findById(userUuid).orElseThrow()
        return participantRepository.findByUser(user).map { it.conversation }
    }

    // Tạo hoặc lấy phòng chat cho Task (để tương thích với tính năng cũ)
    @Transactional
    fun getOrCreateTaskConversation(taskUuid: UUID): ConversationEntity {
        return conversationRepository.findByTaskUuid(taskUuid).orElseGet {
            val newConversation = conversationRepository.save(
                ConversationEntity(type = ConversationType.TASK, taskUuid = taskUuid)
            )
            newConversation
        }
    }

    // Tạo phòng chat mới (Direct hoặc Group)
    @Transactional
    fun createConversation(type: ConversationType, name: String?, participantIds: List<UUID>): ConversationEntity {

        // --- 💡 BƯỚC 1: KIỂM TRA CHỐNG TRÙNG LẶP CHO CHAT 1:1 ---
        if (type == ConversationType.DIRECT && participantIds.size == 2) {
            val user1Id = participantIds[0]
            val user2Id = participantIds[1]

            // Lấy danh sách các cuộc hội thoại hiện tại của User 1
            val user1Conversations = getUserConversations(user1Id)

            // Tìm xem trong đó có đoạn chat DIRECT nào đang chứa User 2 không
            val existingDirectChat = user1Conversations.find { chat ->
                chat.type == ConversationType.DIRECT &&
                        // Gọi 'participants' - chính là cái Getter ảo chúng ta đã định nghĩa hôm trước
                        chat.participants.any { it.uuid == user2Id }
            }

            // Nếu đã từng nhắn tin, trả về luôn phòng chat cũ thay vì tạo mới
            if (existingDirectChat != null) {
                return existingDirectChat
            }
        }
        // --------------------------------------------------------

        // --- 💡 BƯỚC 2: TẠO PHÒNG MỚI (Nếu chưa có hoặc là Nhóm) ---
        val conversation = conversationRepository.save(
            ConversationEntity(type = type, name = name)
        )

        participantIds.forEach { userId ->
            val user = userRepository.findById(userId).orElseThrow()
            participantRepository.save(
                ConversationParticipantEntity(conversation = conversation, user = user)
            )
        }

        return conversation
    }

    fun getConversationById(uuid: UUID): ConversationEntity? {
        return conversationRepository.findById(uuid).orElse(null)
    }

    // Tìm phòng chat bằng Task UUID
    fun getConversationByTaskId(taskUuid: UUID): ConversationEntity? {
        return conversationRepository.findByTaskUuid(taskUuid).orElse(null)
    }

    // Tự động cấp quyền (thêm user vào phòng chat) nếu họ chưa có mặt
    @Transactional
    fun addParticipantIfNotExists(conversationId: UUID, userId: UUID) {
        val conversation = conversationRepository.findById(conversationId).orElseThrow()
        val user = userRepository.findById(userId).orElseThrow()

        val exists = participantRepository.findByConversationAndUser(conversation, user).isPresent
        if (!exists) {
            participantRepository.save(
                ConversationParticipantEntity(conversation = conversation, user = user)
            )
        }
    }
}