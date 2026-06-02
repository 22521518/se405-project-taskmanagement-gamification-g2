@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.data.remote

import com.apollographql.apollo.ApolloClient
import com.example.se405.android.features.tasks_management.data.remote.toLocalDateTimeOrNow
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.ProjectMember
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceRole
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import com.example.se405.android.graphql.GetMembersByProjectQuery
import com.example.se405.android.graphql.GetWorkspaceActivitiesQuery
import com.example.se405.android.graphql.GetWorkspaceQuery
import com.example.se405.android.graphql.type.PaginationInput
import com.apollographql.apollo.api.Optional
import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.features.tasks_management.data.remote.toUuidOrNull
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.graphql.AddMemberToProjectMutation
import com.example.se405.android.graphql.AddMemberToWorkspaceMutation
import com.example.se405.android.graphql.AddMembersToProjectMutation
import com.example.se405.android.graphql.AddMembersToWorkspaceMutation
import com.example.se405.android.graphql.CreateProjectMutation
import com.example.se405.android.graphql.CreateWorkspaceMutation
import com.example.se405.android.graphql.GetProjectQuery
import com.example.se405.android.graphql.GetUsersExcludingQuery
import com.example.se405.android.graphql.GetUsersExcludingQuery.GetUsersExcluding
import com.example.se405.android.graphql.GetWorkspaceListByUserIdQuery
import com.example.se405.android.graphql.type.CreateProjectInput
import com.example.se405.android.graphql.type.CreateWorkspaceInput
import com.example.se405.android.graphql.type.TaskStatus
import com.example.se405.android.graphql.type.TaskType
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GetCreateWorkspaceByWorkspaceIdApiImpl(private val apolloClient: ApolloClient): CreateGetWorkspaceByWorkspaceIdApi {
    override suspend fun getUsersExcluding(userId: Uuid): List<GetUsersExcluding> {
        val response = apolloClient.query(GetUsersExcludingQuery(excludedUserId = userId.toString())).execute()
        println("Errors: ${response.errors}")
        if (!response.errors.isNullOrEmpty()) {
            return emptyList()
        }

        return response.data?.getUsersExcluding ?: emptyList()
    }

    override suspend fun addMembersToWorkspace(
        workspaceId: Uuid,
        userIds: List<Uuid>,
        role: com.example.se405.android.graphql.type.WorkspaceRole
    ): List<AddMembersToWorkspaceMutation.AddMembersToWorkspace> {
        val response = apolloClient.mutation(
            AddMembersToWorkspaceMutation(
                workspaceId = workspaceId.toString(),
                userIds = userIds.map { it.toString() },
                role = role
            )
        ).execute()
        println("Errors: ${response.errors}")
        if (!response.errors.isNullOrEmpty()) return emptyList()

        val members = response.data?.addMembersToWorkspace ?: return emptyList()
        return members
    }

    override suspend fun addMembersToProject(
        projectId: Uuid,
        userIds: List<Uuid>
    ): List<AddMembersToProjectMutation.AddMembersToProject> {
        val response = apolloClient.mutation(
            AddMembersToProjectMutation(
                projectId = projectId.toString(),
                userIds = userIds.map { it.toString() }
            )
        ).execute()
        println("Errors: ${response.errors}")
        if (!response.errors.isNullOrEmpty()) return emptyList()

        val members = response.data?.addMembersToProject ?: return emptyList()
        return members
    }

    override suspend fun addMemberToWorkspace(
        workspaceId: Uuid,
        userId: Uuid,
        role: com.example.se405.android.graphql.type.WorkspaceRole
    ): java.util.Optional<AddMemberToWorkspaceMutation.AddMemberToWorkspace> {
        val response = apolloClient.mutation(
            AddMemberToWorkspaceMutation(
                workspaceId = workspaceId.toString(),
                userId = userId.toString(),
                role = role
            )
        ).execute()
        println("Errors: ${response.errors}")
        if (!response.errors.isNullOrEmpty()) return java.util.Optional.empty()

        val member = response.data?.addMemberToWorkspace ?: return java.util.Optional.empty()
        return java.util.Optional.of(member)
    }

    override suspend fun addMemberToProject(
        projectId: Uuid,
        userId: Uuid
    ): java.util.Optional<AddMemberToProjectMutation.AddMemberToProject> {
        val response = apolloClient.mutation(
            AddMemberToProjectMutation(
                projectId = projectId.toString(),
                userId = userId.toString()
            )
        ).execute()
        println("Errors: ${response.errors}")
        if (!response.errors.isNullOrEmpty()) return java.util.Optional.empty()

        val member = response.data?.addMemberToProject ?: return java.util.Optional.empty()
        return java.util.Optional.of(member)
    }

    override suspend fun createProject(userId: Uuid, workspaceId: Uuid, projectName: String): java.util.Optional<CreateProjectMutation.CreateProject> {
        val response = apolloClient.mutation(CreateProjectMutation(CreateProjectInput(name = projectName, creatorId = userId.toString(), workspaceId = workspaceId.toString()))).execute()
        println("Errors: ${response.errors}")
        if(!response.errors.isNullOrEmpty()) {
            return java.util.Optional.empty()
        }
        val project = response.data?.createProject ?: return java.util.Optional.empty()
        return java.util.Optional.of(project)
    }

    override suspend fun createWorkspace(userId: Uuid, workspaceName: String): java.util.Optional<Workspace> {
        val response = apolloClient.mutation(CreateWorkspaceMutation(CreateWorkspaceInput(name = workspaceName, ownerId =  userId.toString()))).execute()
        println("Errors: ${response.errors}")
        if (!response.errors.isNullOrEmpty()) {
            return java.util.Optional.empty()
        }
        val workspace = response.data?.createWorkspace ?: throw IllegalStateException("No response data")
        return java.util.Optional.of(Workspace(
            id = Uuid.parse(workspace.uuid),
            name = workspace.name,
            projects = emptyList(),
            members = emptyList()
        ))
    }

    override suspend fun getWorkspaceListByUserId(userId: Uuid): List<Workspace> {
        val response = apolloClient.query(GetWorkspaceListByUserIdQuery(userId.toString())).execute()
        println("Errors: ${response.errors}")
        if (!response.errors.isNullOrEmpty()) {
            return emptyList()
        }
        val workspaces = response.data?.getWorkspaceListByUserId ?: return emptyList()
        return workspaces.map { ws ->
            Workspace(
                id = Uuid.parse(ws.uuid),
                name = ws.name,
                projects = ws.projects.map { project -> Project(
                    id = Uuid.parse(project.uuid),
                    name = project.name,
                    workspaceId = Uuid.parse(ws.uuid),
                    tasks = emptyList(),
                    member = emptyList()) },
                members = ws.members.map {
                    WorkspaceMember(
                        userId = Uuid.parse(it.userId),
                        role = it.role.toWorkspaceRole(),
                        joinedAt = it.joinedAt.toLocalDateTimeOrNow(),
                        workspaceId = Uuid.parse(ws.uuid),
                        user = it.user?.let { u ->
                            User(
                                uuid = Uuid.parse(it.userId),
                                email = u.email,
                                username = u.username,
                                displayName = u.displayName,
                                avatarUrl = u.avatarUrl,
                                passwordHash = null,
                                createdAt = null,
                                updatedAt = null
                            )
                        })
                })
        }
    }

    override suspend fun getWorkspaceActivity(workspaceId: Uuid, page: Int?, size: Int?): WorkspaceActivityPage {
        val response = apolloClient.query(GetWorkspaceActivitiesQuery(workspaceId.toString(), pagination = if (page != null && size != null) {
            Optional.present(PaginationInput(page = Optional.present(page), size = Optional.present(size)))
        } else {
            Optional.Absent
        })).execute()
        println("Errors: ${response.errors}")
        val data = response.data
            ?: throw IllegalStateException("No response data")

        val result = data.getWorkspaceActivities

        return WorkspaceActivityPage(
            content = result.content.map {
                TaskCompletionLog(
                    taskCompletionId = Uuid.parse(it.taskCompletionId),
                    completedAt = it.completedAt.toLocalDateTimeOrNow(),
                    status = when(it.status) {
                        "DONE" -> com.example.se405.android.features.tasks_management.domain.entity.TaskStatus.DONE
                        "FAILED" -> com.example.se405.android.features.tasks_management.domain.entity.TaskStatus.FAILED
                        else -> com.example.se405.android.features.tasks_management.domain.entity.TaskStatus.TODO
                    } ,
                    taskId = Uuid.parse(it.task.uuid),
                    userId = Uuid.parse(it.user.uuid),
                    taskTitle = it.task.title,
                    userDisplayName = it.user.displayName,
                    date = null
                )
            },
            pageInfo = PageInfo(
                totalElements = result.pageInfo.totalElements,
                totalPages = result.pageInfo.totalPages,
                currentPage = result.pageInfo.currentPage,
                pageSize = result.pageInfo.pageSize,
                hasNext = result.pageInfo.hasNext,
                hasPrevious = result.pageInfo.hasPrevious,
            )
        )
    }

    override suspend fun getMemberByProject(projectId: Uuid): List<ProjectMember> {
        val response = apolloClient.query(GetMembersByProjectQuery(projectId.toString())).execute()
        println("Errors: ${response.errors}")
        if(!response.errors.isNullOrEmpty()) {
            return emptyList()
        }
        val members = response.data?.getMembersByProject ?: return emptyList()
        return members.map {
            it.toProjectMember()
        }
    }

    private fun GetMembersByProjectQuery.GetMembersByProject.toProjectMember(): ProjectMember {
        return ProjectMember(
            projectId = Uuid.parse(this.projectId),
            workspaceId = Uuid.parse(this.workspaceId),
            userId = Uuid.parse(this.userId),
            joinedAt = this.joinedAt.toLocalDateTimeOrNow(),
            user = this.user?.toUser(),
        )
    }

    private fun GetMembersByProjectQuery.User.toUser(): User {
        return User(
            uuid = this.uuid.let(Uuid::parse),
            username = this.username,
            email = this.email,
            displayName = this.displayName,
            avatarUrl = this.avatarUrl,
            createdAt = null,
            updatedAt = null,
            passwordHash = null
        )
    }

   override suspend fun getWorkspaceById(workspaceId: Uuid): java.util.Optional<Workspace> {
       val response = apolloClient.query(GetWorkspaceQuery(workspaceId.toString())).execute()
       println("Errors: ${response.errors}")
       if (!response.errors.isNullOrEmpty()) {
           return java.util.Optional.empty()
       }
       val workspace = response.data?.getWorkspace ?: return java.util.Optional.empty()
       return java.util.Optional.of(Workspace(
           id = Uuid.parse(workspace.uuid),
           name = workspace.name,
           projects = workspace.projects.map { it.toProject()},
           members = workspace.members.map { mem ->
               WorkspaceMember(
                   workspaceId = Uuid.parse(mem.workspaceId),
                   userId = Uuid.parse(mem.userId),
                   role = mem.role.toWorkspaceRole(),
                   joinedAt = mem.joinedAt.toLocalDateTimeOrNow(),
                   user = if(mem.user != null) User(
                       uuid = Uuid.parse(mem.user.uuid), // Now guaranteed non-null
                       username = mem.user.username,
                       displayName = mem.user.displayName,
                       email = mem.user.email,
                       avatarUrl = mem.user.avatarUrl
                   ) else null
               )
           }
       ))
   }

    // =============================== __PROJECT_DETAIL__ =====================================
    override suspend fun getProjectByProjectId(projectId: Uuid): java.util.Optional<GetProjectQuery.GetProject> {
        val response = apolloClient.query(GetProjectQuery(projectId.toString())).execute()
        println("Errors: ${response.errors}")
        if (!response.errors.isNullOrEmpty()) {
            return java.util.Optional.empty()
        }
        val project = response.data?.getProject
            ?: return java.util.Optional.empty()

        val today = LocalDate.now()
        val updatedTasks = project.tasks.map { task ->
            val updatedStatus = computeTaskStatus(task, today)
            task.copy(status = updatedStatus)
        }

        return java.util.Optional.of(project.copy(tasks = updatedTasks))
    }

    private fun computeTaskStatus(
        task: GetProjectQuery.Task,
        targetDate: LocalDate
    ): TaskStatus {
        val taskLogs = task.taskCompletionLogs

        return when (task.type) {
            TaskType.HABIT -> {
                val logsForDate = taskLogs.filter { log ->
                    log.completedAt.let { LocalDate.parse(it) } == targetDate
                }
                val completedCount = logsForDate.count { it.status == TaskStatus.DONE.rawValue }
                val requiredCount = if ((task.repetition ?: 0) <= 0) 1 else task.repetition!!
                when {
                    completedCount >= requiredCount -> TaskStatus.DONE
                    logsForDate.any { it.status == TaskStatus.FAILED.rawValue } -> TaskStatus.FAILED
                    else -> TaskStatus.TODO
                }
            }
            else -> {
                val start = task.startDate?.let { LocalDate.parse(it) }
                    ?: task.dueDate?.let { LocalDate.parse(it) }
                    ?: return TaskStatus.TODO
                val end = task.dueDate?.let { LocalDate.parse(it) }
                    ?: task.startDate?.let { LocalDate.parse(it) }
                    ?: return TaskStatus.TODO

                val currentStatus = taskLogs.firstOrNull()?.status

                when {
                    targetDate.isBefore(start) -> currentStatus?.toTaskStatus() ?: TaskStatus.TODO
                    targetDate.isAfter(start) && targetDate.isBefore(end) ->
                        currentStatus?.toTaskStatus() ?: TaskStatus.IN_PROGRESS
                    else -> currentStatus?.toTaskStatus() ?: TaskStatus.FAILED
                }
            }
        }
    }

    private fun String.toTaskStatus(): TaskStatus = TaskStatus.safeValueOf(this)

    private fun com.example.se405.android.graphql.type.WorkspaceRole.toWorkspaceRole(): WorkspaceRole =
        when (this) {
            com.example.se405.android.graphql.type.WorkspaceRole.OWNER -> WorkspaceRole.OWNER
            com.example.se405.android.graphql.type.WorkspaceRole.MEMBER -> WorkspaceRole.MEMBER
            else -> WorkspaceRole.MEMBER
        }

    private fun GetWorkspaceQuery.Project.toProject(): Project =
        Project(
            id = Uuid.parse(this.uuid),
            name = this.name,
            createdAt = this.createdAt.toLocalDateTimeOrNow(),
            tasks = this.tasks.map { task ->
                Task(
                    uuid = Uuid.parse(task.uuid),
                    title = task.title,
                    description = task.description ?: "",
                    type = com.example.se405.android.features.tasks_management.domain.entity.TaskType.PROJECT,
                    status = com.example.se405.android.features.tasks_management.domain.entity.TaskStatus.TODO,
                    taskCompletionLog = task.taskCompletionLogs.map { log ->
                        TaskCompletionLog(
                            taskCompletionId = null,
                            status = when (log.status) {
                                "DONE" -> com.example.se405.android.features.tasks_management.domain.entity.TaskStatus.DONE
                                "FAILED" -> com.example.se405.android.features.tasks_management.domain.entity.TaskStatus.FAILED
                                else -> com.example.se405.android.features.tasks_management.domain.entity.TaskStatus.TODO
                            },
                            date = null,
                            completedAt = null,
                            taskId = null,
                            userId = null
                        )
                    },
                    startDate = task.startDate.toLocalDateTimeOrNow().toLocalDate(),
                    dueDate = task.dueDate.toLocalDateTimeOrNow().toLocalDate(),
                    projectId = Uuid.parse(this.uuid),
                    repetition = 0,
                    priority = when (task.priority) {
                        com.example.se405.android.graphql.type.TaskPriority.HIGH -> TaskPriority.HIGH
                        com.example.se405.android.graphql.type.TaskPriority.MEDIUM -> TaskPriority.MEDIUM
                        com.example.se405.android.graphql.type.TaskPriority.LOW -> TaskPriority.LOW
                        else -> TaskPriority.MEDIUM
                    },
                    creator = null,
                    // A workspace task may carry both WORKSPACE and PERSONAL tags. Map each
                    // tag's real ownership and skip any tag that violates the Tag invariant
                    // so one malformed tag can't blank out the whole workspace screen.
                    tags = task.tags.mapNotNull { tag ->
                        runCatching { tag.toDomainTag(task.uuid) }
                            .onFailure {
                                android.util.Log.e(
                                    "WS_DETAIL_LOAD",
                                    "Skipping invalid tag ${tag.uuid} on task ${task.uuid}",
                                    it,
                                )
                            }
                            .getOrNull()
                    },
                    assignees = task.assignees.map { assignee ->
                        User(
                            uuid = Uuid.parse(assignee.user.uuid),
                            email = assignee.user.email,
                            username = assignee.user.username,
                            displayName = assignee.user.displayName,
                            avatarUrl = assignee.user.avatarUrl,
                            passwordHash = null,
                            createdAt = null,
                            updatedAt = null
                        )
                    },
                ).getTaskStatus(LocalDate.now())
            },
            member = this.members.map { ProjectMember(
                projectId = Uuid.parse(this.uuid),
                workspaceId = Uuid.parse(this.workspaceId),
                userId = Uuid.parse(it.userId),
                joinedAt = null,
                user = User(
                    uuid = Uuid.parse(it.userId),
                    username = it.user?.username,
                    displayName = it.user?.displayName,
                    email = it.user?.email,
                    avatarUrl = it.user?.avatarUrl,
                    passwordHash = null,
                    createdAt = null,
                    updatedAt = null
                )
            ) },
        )

    private fun GetWorkspaceQuery.Tag.toDomainTag(parentTaskUuid: String): Tag {
        val targetLabelUuid = this.label.uuid
        val resolvedLabel = BuiltinLabels.find { builtin ->
            builtin.id.toString() == targetLabelUuid
        } ?: BuiltinLabels.first()

        // Respect the tag's real ownership instead of assuming WORKSPACE: a workspace
        // task can also have PERSONAL tags attached, which must not carry a workspaceId.
        val resolvedOwnership = when (ownershipType) {
            com.example.se405.android.graphql.type.TagOwnershipType.WORKSPACE -> TagOwnershipType.WORKSPACE
            else -> TagOwnershipType.PERSONAL
        }
        val resolvedWorkspaceId =
            if (resolvedOwnership == TagOwnershipType.WORKSPACE) workspaceId.toUuidOrNull() else null

        return Tag(
            createdBy = null,
            ownershipType = resolvedOwnership,
            workspaceId = resolvedWorkspaceId,
            taskIds = listOfNotNull(parentTaskUuid.toUuidOrNull()),
            uuid = Uuid.parse(uuid),
            name = name,
            color = color,
            label = resolvedLabel,
            creator = null,
        )
    }
}