package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.WorkspaceMemberEntity
import com.example.se405.backend.database.model.WorkspaceMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WorkspaceMemberRepository : JpaRepository<WorkspaceMemberEntity, WorkspaceMemberId> {
    fun findByWorkspaceId(workspaceId: UUID): List<WorkspaceMemberEntity>
    fun findByUserId(userId: UUID): List<WorkspaceMemberEntity>
}
