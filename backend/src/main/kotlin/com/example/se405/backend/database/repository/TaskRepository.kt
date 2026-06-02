package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.TaskEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TaskRepository : JpaRepository<TaskEntity, UUID> {
    fun findByCreatorId(creatorId: UUID): List<TaskEntity>
    fun findByProjectId(projectId: UUID): List<TaskEntity>

    @Query("""
    SELECT t FROM TaskEntity t
    LEFT JOIN FETCH t.assignees
    WHERE t.uuid = :id
""")
    fun findByIdWithAssignees(id: UUID): TaskEntity?

    @Query("""
    SELECT DISTINCT t FROM TaskEntity t
    LEFT JOIN FETCH t.assignees ta
    LEFT JOIN FETCH ta.user u
    WHERE 
        (t.type = 'HABIT' AND t.creatorId = :userId)
        OR
        (t.type = 'PROJECT' AND u.uuid = :userId)
""")
    fun findTaskResponsibilitiesByUserIdWithAssignees(userId: UUID): List<TaskEntity>

    @Query("""
    SELECT DISTINCT t FROM TaskEntity t
    LEFT JOIN FETCH t.assignees ta
    LEFT JOIN FETCH ta.user u
    WHERE
        (t.type = 'HABIT' AND t.creatorId = :userId)
        OR
        (t.type = 'PROJECT' AND u.uuid = :userId)
""")
    fun findPersonalTasksByUserIdWithDetails(userId: UUID): List<TaskEntity>

//    @Query("""
//    SELECT t FROM TaskEntity t
//    LEFT JOIN FETCH t.assignees ta
//    LEFT JOIN FETCH ta.user
//""")
//    fun findAllWithAssignees(): List<TaskEntity>
}
