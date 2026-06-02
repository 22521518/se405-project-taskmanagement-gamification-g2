package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.TagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface TagRepository : JpaRepository<TagEntity, UUID> {
    fun findByWorkspaceId(workspaceId: UUID): List<TagEntity>
    fun findByCreatedBy(createdBy: UUID): List<TagEntity>

    /** Find all tags associated with a specific task via the task_tags junction table */
    @Query("SELECT t FROM TagEntity t JOIN t.tasks task WHERE task.uuid = :taskId")
    fun findByTaskId(@Param("taskId") taskId: UUID): List<TagEntity>

    /** Remove all task_tags junction entries for a given tag before deletion */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM task_tags WHERE tag_id = :tagId", nativeQuery = true)
    fun deleteTaskTagsByTagId(@Param("tagId") tagId: UUID)
}
