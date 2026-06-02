package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.ProjectEntity
import com.example.se405.backend.database.model.ProjectMemberEntity
import com.example.se405.backend.database.model.ProjectMemberId
import com.example.se405.backend.database.model.TaskCompletionLogEntity
import com.example.se405.backend.database.model.TaskEntity
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.model.WorkspaceEntity
import com.example.se405.backend.database.model.WorkspaceMemberEntity
import com.example.se405.backend.database.model.WorkspaceMemberId
import com.example.se405.backend.database.model.WorkspaceRole
import com.example.se405.backend.database.repository.ProjectMemberRepository
import com.example.se405.backend.database.repository.ProjectRepository
import com.example.se405.backend.database.repository.TaskCompletionLogRepository
import com.example.se405.backend.database.repository.TaskRepository
import com.example.se405.backend.database.repository.UserRepository
import com.example.se405.backend.database.repository.WorkspaceMemberRepository
import com.example.se405.backend.database.repository.WorkspaceRepository
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Controller
class WorkspaceController(
    private val workspaceRepository: WorkspaceRepository,
    private val projectRepository: ProjectRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val taskCompletionLogRepository: TaskCompletionLogRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    // ── Queries ───────────────────────────────────────────────────────────────

    @QueryMapping
    fun getWorkspaceListByUserId(@Argument userId: UUID): List<WorkspaceEntity> {
        val res = workspaceMemberRepository
            .findByIdUserId(userId)
        log.info("(${LocalDate.now()}) == $res")
        return res;
    }

    @QueryMapping
    fun getWorkspace(@Argument uuid: UUID): WorkspaceEntity? {
        return workspaceRepository.findById(uuid).orElse(null)
    }

    @QueryMapping
    fun getProject(@Argument uuid: UUID): ProjectEntity? {
        return projectRepository.findById(uuid).orElse(null)
    }

    @QueryMapping
    fun getProjectsByWorkspace(@Argument workspaceId: UUID): List<ProjectEntity> =
        projectRepository.findByWorkspaceUuid(workspaceId)

    @QueryMapping
    fun getMembersByWorkspace(@Argument workspaceId: UUID): List<WorkspaceMemberEntity> =
        workspaceMemberRepository.findByIdWorkspaceId(workspaceId)

    @QueryMapping
    fun getMembersByProject(@Argument projectId: UUID): List<ProjectMemberEntity> {
        return projectMemberRepository.findByIdProjectId(projectId)
    }

    @QueryMapping
    fun getWorkspaceActivities(
        @Argument uuid: UUID,
        @Argument pagination: PaginationInput?,
    ): WorkspaceActivityPage {

        val projects = projectRepository.findByWorkspaceUuid(uuid)
        val taskIds = projects
            .flatMap { project ->
                project.uuid?.let { taskRepository.findByProjectId(it) } ?: emptyList()
            }
            .mapNotNull { it.uuid }

        if (taskIds.isEmpty()) {
            return WorkspaceActivityPage(
                content = emptyList(),
                pageInfo = PageInfo(
                    totalElements = 0,
                    totalPages = 0,
                    currentPage = 0,
                    pageSize = 0,
                    hasNext = false,
                    hasPrevious = false,
                )
            )
        }

        val allLogs: List<TaskCompletionLogEntity> =
            taskCompletionLogRepository.findByTaskIdInOrderByCompletedAtDesc(taskIds)

        val total = allLogs.size
        val page = pagination?.page
        val size = pagination?.size

        if (page == null || size == null) {
            return WorkspaceActivityPage(
                content = allLogs,
                pageInfo = PageInfo(
                    totalElements = total,
                    totalPages = 1,
                    currentPage = 0,
                    pageSize = total,
                    hasNext = false,
                    hasPrevious = false,
                )
            )
        }

        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceAtLeast(1)
        val totalPages = if (total == 0) 1 else (total + safeSize - 1) / safeSize

        val fromIndex = (safePage * safeSize).coerceAtMost(total)
        val toIndex   = (fromIndex + safeSize).coerceAtMost(total)

        return WorkspaceActivityPage(
            content = allLogs.subList(fromIndex, toIndex),
            pageInfo = PageInfo(
                totalElements = total,
                totalPages    = totalPages,
                currentPage   = safePage,
                pageSize      = safeSize,
                hasNext       = safePage < totalPages - 1,
                hasPrevious   = safePage > 0,
            )
        )
    }

    // ── Resolve nested fields on Workspace ───────────────────────────────────
    @SchemaMapping(typeName = "Workspace", field = "owner")
    fun workspaceOwner(workspace: WorkspaceEntity): UserEntity? = workspace.owner

    @SchemaMapping(typeName = "Workspace", field = "projects")
    fun workspaceProjects(workspace: WorkspaceEntity): List<ProjectEntity> {
        if(workspace.uuid == null) return emptyList()
        return projectRepository.findByWorkspaceUuid(workspace.uuid)
    }

    @SchemaMapping(typeName = "Workspace", field = "members")
    fun workspaceMembers(workspace: WorkspaceEntity): List<WorkspaceMemberEntity> {
        if(workspace.uuid == null) return emptyList()
        return workspaceMemberRepository.findByIdWorkspaceId(workspace.uuid)
    }

    // ── Resolve nested fields on WorkspaceMember ─────────────────────────────

    @SchemaMapping(typeName = "WorkspaceMember", field = "user")
    fun memberUser(member: WorkspaceMemberEntity): UserEntity? = member.user
//        userRepository.findById(member.userId).orElse(null)

    // ── Resolve nested fields on ProjectMember ─────────────────────────────

    @SchemaMapping(typeName = "Project", field = "members")
    fun projectMembers(project: ProjectEntity): List<ProjectMemberEntity> {
        if (project.uuid == null) return emptyList()
        return projectMemberRepository.findByIdProjectId(project.uuid)
    }

    @SchemaMapping(typeName = "ProjectMember", field = "user")
    fun projectMemberUser(pm: ProjectMemberEntity): UserEntity? {
        return pm.workspaceMember.user
    }

    // ── Resolve nested fields on Project ─────────────────────────────────────

    @SchemaMapping(typeName = "Project", field = "workspace")
    fun projectWorkspace(project: ProjectEntity): WorkspaceEntity? = project.workspace

    @SchemaMapping(typeName = "Project", field = "tasks")
    fun projectTasks(project: ProjectEntity): List<TaskEntity> {
        if (project.uuid == null) return emptyList()
        return taskRepository.findByProjectId(project.uuid)
    }

    // ── Project.workspaceId ───────────────────────────────────────
    @SchemaMapping(typeName = "Project", field = "workspaceId")
    fun projectWorkspaceId(project: ProjectEntity): UUID? = project.workspace.uuid

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Transactional
    @MutationMapping
    fun createWorkspace(@Argument input: CreateWorkspaceInput): WorkspaceEntity {
        val ownerRef = userRepository.getReferenceById(input.ownerId)
        val newWorkspace = WorkspaceEntity(name = input.name, owner = ownerRef)
        val ws = workspaceRepository.save(newWorkspace)
        workspaceMemberRepository.save(WorkspaceMemberEntity(
            id = WorkspaceMemberId(workspaceId = ws.uuid!!, userId = input.ownerId),
            user = ownerRef,
            workspace = ws,
            role = WorkspaceRole.OWNER
        ))
        return ws
    }

    @Transactional
    @MutationMapping
    fun createProject(@Argument input: CreateProjectInput): ProjectEntity {
        val workspaceRef = workspaceRepository.getReferenceById(input.workspaceId)
        val userRef = userRepository.getReferenceById(input.creatorId)

        val newProject = projectRepository.save(
            ProjectEntity(name = input.name, workspace = workspaceRef)
        )

        val wm = workspaceMemberRepository.findById(
            WorkspaceMemberId(workspaceId = input.workspaceId, userId = input.creatorId)
        ).orElseThrow { RuntimeException("Creator is not a member of the workspace") }

        projectMemberRepository.save(
            ProjectMemberEntity(
                id = ProjectMemberId(
                    projectId = newProject.uuid!!,
                    workspaceId = input.workspaceId,
                    userId = input.creatorId
                ),
                project = newProject,
                workspaceMember = wm,
                user = userRef
            )
        )

        return newProject
    }

    @Transactional
    @MutationMapping
    fun deleteWorkspace(@Argument uuid: UUID): WorkspaceEntity {
        val existing = workspaceRepository.findById(uuid)
            .orElseThrow { RuntimeException("Workspace not found") }

        val projects = projectRepository.findByWorkspaceUuid(uuid)
        for (project in projects) {
            val projectId = project.uuid ?: continue
            val projectMembers = projectMemberRepository.findByIdProjectId(projectId)
            projectMemberRepository.deleteAll(projectMembers)
        }

        projectRepository.deleteAll(projects)

        val workspaceMembers = workspaceMemberRepository.findByIdWorkspaceId(uuid)
        workspaceMemberRepository.deleteAll(workspaceMembers)

        workspaceRepository.delete(existing)
        return existing
    }

    @Transactional
    @MutationMapping
    fun deleteProject(@Argument uuid: UUID): ProjectEntity {
        val existing = projectRepository.findById(uuid)
            .orElseThrow { RuntimeException("Project not found") }
        val members = projectMemberRepository.findByIdProjectId(uuid)
        projectMemberRepository.deleteAll(members)
        projectRepository.delete(existing)
        return existing
    }

    @MutationMapping
    fun addMemberToWorkspace(
        @Argument workspaceId: UUID,
        @Argument userId: UUID,
        @Argument role: WorkspaceRole
    ): WorkspaceMemberEntity {

        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow { RuntimeException("Workspace not found") }

        if (workspaceMemberRepository.existsById(WorkspaceMemberId(workspaceId, userId))) {
            throw RuntimeException("User already in workspace")
        }

        val userRef = userRepository.getReferenceById(userId)

        val entity = WorkspaceMemberEntity(
            id = WorkspaceMemberId(
                workspaceId = workspaceId,
                userId = userId
            ),
            workspace = workspace,
            user = userRef,
            role = role
        )

        return workspaceMemberRepository.save(entity)
    }

    @MutationMapping
    fun addMemberToProject(
        @Argument projectId: UUID,
        @Argument userId: UUID
    ): ProjectMemberEntity {

        val project = projectRepository.findById(projectId)
            .orElseThrow { RuntimeException("Project not found") }

        val workspaceId = project.workspace.uuid
            ?: throw RuntimeException("Workspace not found")

        if (projectMemberRepository.existsById(ProjectMemberId(projectId = projectId, workspaceId = workspaceId, userId = userId))) {
            throw RuntimeException("User already in workspace")
        }

        val userRef = userRepository.getReferenceById(userId)

        val wm = workspaceMemberRepository.findById(
            WorkspaceMemberId(workspaceId, userId)
        ).orElseThrow {
            RuntimeException("User not in workspace")
        }

        val entity = ProjectMemberEntity(
            id = ProjectMemberId(
                projectId = projectId,
                workspaceId = workspaceId,
                userId = userId
            ),
            project = project,
            workspaceMember = wm,
            user = userRef
        )

        return projectMemberRepository.save(entity)
    }

    @Transactional
    @MutationMapping
    fun addMembersToWorkspace(
        @Argument workspaceId: UUID,
        @Argument userIds: List<UUID>,
        @Argument role: WorkspaceRole
    ): List<WorkspaceMemberEntity> {
        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow { RuntimeException("Workspace not found") }

        val existingIds = workspaceMemberRepository
            .findByIdWorkspaceId(workspaceId)
            .map { it.id.userId }
            .toSet()

        val newIds = userIds.filter { it !in existingIds }
        if (newIds.isEmpty()) throw RuntimeException("All users are already in workspace")

        val entities = newIds.map { userId ->
            WorkspaceMemberEntity(
                id = WorkspaceMemberId(workspaceId = workspaceId, userId = userId),
                workspace = workspace,
                user = userRepository.getReferenceById(userId),
                role = role
            )
        }

        return workspaceMemberRepository.saveAll(entities)
    }

    @Transactional
    @MutationMapping
    fun addMembersToProject(
        @Argument projectId: UUID,
        @Argument userIds: List<UUID>
    ): List<ProjectMemberEntity> {
        val project = projectRepository.findById(projectId)
            .orElseThrow { RuntimeException("Project not found") }

        val workspaceId = project.workspace.uuid
            ?: throw RuntimeException("Workspace not found")

        val existingIds = projectMemberRepository
            .findByIdProjectId(projectId)
            .map { it.id.userId }
            .toSet()

        val newIds = userIds.filter { it !in existingIds }
        if (newIds.isEmpty()) throw RuntimeException("All users are already in project")

        val entities = newIds.map { userId ->
            val wm = workspaceMemberRepository.findById(
                WorkspaceMemberId(workspaceId, userId)
            ).orElseThrow { RuntimeException("User $userId is not a member of the workspace") }

            ProjectMemberEntity(
                id = ProjectMemberId(
                    projectId = projectId,
                    workspaceId = workspaceId,
                    userId = userId
                ),
                project = project,
                workspaceMember = wm,
                user = userRepository.getReferenceById(userId)
            )
        }

        return projectMemberRepository.saveAll(entities)
    }

    @Transactional
    @MutationMapping
    fun removeMemberFromWorkspace(
        @Argument workspaceId: UUID,
        @Argument userId: UUID
    ): Boolean {
        val projects = projectRepository.findByWorkspaceUuid(workspaceId)
        for (project in projects) {
            val projectId = project.uuid ?: continue
            val pmId = ProjectMemberId(
                projectId = projectId,
                workspaceId = workspaceId,
                userId = userId
            )
            if (projectMemberRepository.existsById(pmId)) {
                projectMemberRepository.deleteById(pmId)
            }
        }

        val wmId = WorkspaceMemberId(workspaceId = workspaceId, userId = userId)
        if (!workspaceMemberRepository.existsById(wmId)) {
            throw RuntimeException("User not in workspace")
        }
        workspaceMemberRepository.deleteById(wmId)
        return true
    }

    @Transactional
    @MutationMapping
    fun removeMemberFromProject(
        @Argument projectId: UUID,
        @Argument userId: UUID
    ): Boolean {

        val project = projectRepository.findById(projectId)
            .orElseThrow { RuntimeException("Project not found") }

        val workspaceId = project.workspace.uuid
            ?: throw RuntimeException("Workspace not found")

        projectMemberRepository.deleteById(
            ProjectMemberId(projectId, workspaceId, userId)
        )

        return true
    }

    // ── Khắc phục các trường ID cho ProjectMember ─────────────────────────────
    @SchemaMapping(typeName = "ProjectMember", field = "projectId")
    fun projectMemberProjectId(pm: ProjectMemberEntity): UUID = pm.id.projectId

    @SchemaMapping(typeName = "ProjectMember", field = "workspaceId")
    fun projectMemberWorkspaceId(pm: ProjectMemberEntity): UUID = pm.id.workspaceId

    @SchemaMapping(typeName = "ProjectMember", field = "userId")
    fun projectMemberUserId(pm: ProjectMemberEntity): UUID = pm.id.userId

    @SchemaMapping(typeName = "ProjectMember", field = "joinedAt")
    fun projectMemberJoinedAt(pm: ProjectMemberEntity): String = pm.joinedAt.toString()

    // ── Khắc phục các trường ID cho WorkspaceMember ───────────────────────────
    @SchemaMapping(typeName = "WorkspaceMember", field = "workspaceId")
    fun workspaceMemberWorkspaceId(wm: WorkspaceMemberEntity): UUID = wm.id.workspaceId

    @SchemaMapping(typeName = "WorkspaceMember", field = "userId")
    fun workspaceMemberUserId(wm: WorkspaceMemberEntity): UUID = wm.id.userId

    @SchemaMapping(typeName = "WorkspaceMember", field = "joinedAt")
    fun workspaceMemberJoinedAt(wm: WorkspaceMemberEntity): String = wm.joinedAt.toString()
}

data class CreateWorkspaceInput(val name: String, val ownerId: UUID)
//data class CreateProjectInput(val name: String, val workspaceId: UUID)
data class CreateProjectInput(val name: String, val workspaceId: UUID, val creatorId: UUID) // thêm creatorId
data class PaginationInput(val page: Int?, val size: Int?)

data class PageInfo(
    val totalElements: Int,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

data class WorkspaceActivityPage(
    val content: List<TaskCompletionLogEntity>,
    val pageInfo: PageInfo,
)