package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.ProjectMemberEntity
import com.example.se405.backend.database.model.ProjectMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProjectMemberRepository : JpaRepository<ProjectMemberEntity, ProjectMemberId> {
    fun findByIdWorkspaceId(workspaceId: UUID): List<ProjectMemberEntity>
    fun findByIdUserId(userId: UUID): List<ProjectMemberEntity>
    fun findByIdProjectId(projectId: UUID): List<ProjectMemberEntity>
}
