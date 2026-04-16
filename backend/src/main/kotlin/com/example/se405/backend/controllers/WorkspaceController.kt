package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.ProjectEntity
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.model.WorkspaceEntity
import com.example.se405.backend.database.model.WorkspaceMemberEntity
import com.example.se405.backend.database.repository.ProjectRepository
import com.example.se405.backend.database.repository.UserRepository
import com.example.se405.backend.database.repository.WorkspaceMemberRepository
import com.example.se405.backend.database.repository.WorkspaceRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class WorkspaceController(
    private val workspaceRepository: WorkspaceRepository,
    private val projectRepository: ProjectRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
    private val userRepository: UserRepository,
) {

    // ── Queries ───────────────────────────────────────────────────────────────

    @QueryMapping
    fun getWorkspace(@Argument uuid: UUID): WorkspaceEntity? =
        workspaceRepository.findById(uuid).orElse(null)

    @QueryMapping
    fun getProjectsByWorkspace(@Argument workspaceId: UUID): List<ProjectEntity> =
        projectRepository.findByWorkspaceId(workspaceId)

    @QueryMapping
    fun getMembersByWorkspace(@Argument workspaceId: UUID): List<WorkspaceMemberEntity> =
        workspaceMemberRepository.findByWorkspaceId(workspaceId)

    // ── Resolve nested fields on Workspace ───────────────────────────────────

    @SchemaMapping(typeName = "Workspace", field = "projects")
    fun workspaceProjects(workspace: WorkspaceEntity): List<ProjectEntity> {
        if(workspace.uuid == null) return emptyList()
        return projectRepository.findByWorkspaceId(workspace.uuid)
    }

    @SchemaMapping(typeName = "Workspace", field = "members")
    fun workspaceMembers(workspace: WorkspaceEntity): List<WorkspaceMemberEntity> {
        if(workspace.uuid == null) return emptyList()
        return workspaceMemberRepository.findByWorkspaceId(workspace.uuid)
    }

    // ── Resolve nested fields on WorkspaceMember ─────────────────────────────

    @SchemaMapping(typeName = "WorkspaceMember", field = "user")
    fun memberUser(member: WorkspaceMemberEntity): UserEntity? =
        userRepository.findById(member.userId).orElse(null)

    // ── Resolve nested fields on Project ─────────────────────────────────────

    @SchemaMapping(typeName = "Project", field = "workspace")
    fun projectWorkspace(project: ProjectEntity): WorkspaceEntity? =
        workspaceRepository.findById(project.workspaceId).orElse(null)

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    fun createWorkspace(@Argument input: CreateWorkspaceInput): WorkspaceEntity =
        workspaceRepository.save(WorkspaceEntity(name = input.name))

    @MutationMapping
    fun createProject(@Argument input: CreateProjectInput): ProjectEntity =
        projectRepository.save(ProjectEntity(name = input.name, workspaceId = input.workspaceId))

    @MutationMapping
    fun deleteWorkspace(@Argument uuid: UUID): WorkspaceEntity {
        val existing = workspaceRepository.findById(uuid)
            .orElseThrow { RuntimeException("Workspace not found") }
        workspaceRepository.delete(existing)
        return existing
    }

    @MutationMapping
    fun deleteProject(@Argument uuid: UUID): ProjectEntity {
        val existing = projectRepository.findById(uuid)
            .orElseThrow { RuntimeException("Project not found") }
        projectRepository.delete(existing)
        return existing
    }
}

data class CreateWorkspaceInput(val name: String)
data class CreateProjectInput(val name: String, val workspaceId: UUID)
