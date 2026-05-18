package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.ConversationEntity
import com.example.se405.backend.database.model.ConversationParticipantEntity
import com.example.se405.backend.database.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConversationParticipantRepository : JpaRepository<ConversationParticipantEntity, UUID> {

    // Lấy tất cả các "phòng" mà một User đang tham gia
    fun findByUser(user: UserEntity): List<ConversationParticipantEntity>

    // Tìm xem một User có trong phòng chat cụ thể nào đó không
    fun findByConversationAndUser(conversation: ConversationEntity, user: UserEntity): Optional<ConversationParticipantEntity>

    // Lấy danh sách thành viên trong một phòng
    fun findByConversation(conversation: ConversationEntity): List<ConversationParticipantEntity>
}