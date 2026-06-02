package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.TaskEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TaskRepository : JpaRepository<TaskEntity, UUID> {
    fun findByCreatorId(creatorId: UUID): List<TaskEntity>
}
