package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.TaskCompletionLogEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TaskCompletionLogRepository : JpaRepository<TaskCompletionLogEntity, UUID> {
    fun findByTaskId(taskId: UUID): List<TaskCompletionLogEntity>
    fun findByUserId(userId: UUID): List<TaskCompletionLogEntity>
}
