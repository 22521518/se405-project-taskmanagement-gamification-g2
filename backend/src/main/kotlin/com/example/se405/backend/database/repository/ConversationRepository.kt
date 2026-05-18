package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.ConversationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConversationRepository : JpaRepository<ConversationEntity, UUID> {
    fun findByTaskUuid(taskUuid: UUID): Optional<ConversationEntity>
}