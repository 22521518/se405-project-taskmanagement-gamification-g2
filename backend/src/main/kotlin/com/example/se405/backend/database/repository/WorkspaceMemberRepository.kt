package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.WorkspaceEntity
import com.example.se405.backend.database.model.WorkspaceMemberEntity
import com.example.se405.backend.database.model.WorkspaceMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WorkspaceMemberRepository : JpaRepository<WorkspaceMemberEntity, WorkspaceMemberId> {
    fun findByIdWorkspaceId(workspaceId: UUID): List<WorkspaceMemberEntity>
    @Query("""
    SELECT DISTINCT wm.workspace FROM WorkspaceMemberEntity wm
    WHERE wm.id.userId = :userId
""")
    fun findByIdUserId(userId: UUID): List<WorkspaceEntity>
}
