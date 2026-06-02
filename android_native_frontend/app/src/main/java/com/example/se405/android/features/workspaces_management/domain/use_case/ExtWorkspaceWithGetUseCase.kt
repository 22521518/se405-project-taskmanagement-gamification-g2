@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.domain.use_case

import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.ProjectMember
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.tasks_management.domain.repository.TagRepository
import com.example.se405.android.features.tasks_management.domain.repository.TaskRepository
import com.example.se405.android.features.tasks_management.domain.use_case.WorkspaceUseCases
import com.example.se405.android.features.workspaces_management.data.remote.CreateGetWorkspaceByWorkspaceIdApi
import com.example.se405.android.features.workspaces_management.data.remote.WorkspaceActivityPage
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import com.example.se405.android.features.workspaces_management.domain.repository.ExtWorkspaceRepositoryWithGet
import com.example.se405.android.graphql.AddMemberToProjectMutation
import com.example.se405.android.graphql.AddMemberToWorkspaceMutation
import com.example.se405.android.graphql.AddMembersToProjectMutation
import com.example.se405.android.graphql.AddMembersToWorkspaceMutation
import com.example.se405.android.graphql.CreateProjectMutation
import com.example.se405.android.graphql.GetProjectQuery
import com.example.se405.android.graphql.GetUsersExcludingQuery.GetUsersExcluding
import com.example.se405.android.graphql.type.WorkspaceRole
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExtWorkspaceWithGetCreateUseCases(
    private val delegate: WorkspaceUseCases,
    private val tagRepo: TagRepository,
    private val taskRepo: TaskRepository,
//    private val extendedRepo: ExtWorkspaceRepositoryWithGet,
    private val api: CreateGetWorkspaceByWorkspaceIdApi,
) {
    suspend fun getUsersExcluding(userId: Uuid): List<GetUsersExcluding>
        = api.getUsersExcluding(userId)

    suspend fun addMemberToWorkspace(workspaceId: Uuid, userId: Uuid, role: WorkspaceRole = WorkspaceRole.MEMBER): Optional<AddMemberToWorkspaceMutation.AddMemberToWorkspace>
        = api.addMemberToWorkspace(workspaceId, userId, role)

    suspend fun addMemberToProject(projectId: Uuid, userId: Uuid): Optional<AddMemberToProjectMutation.AddMemberToProject>
        = api.addMemberToProject(projectId, userId)

    suspend fun addMembersToWorkspace(workspaceId: Uuid, userIds: List<Uuid>, role: WorkspaceRole = WorkspaceRole.MEMBER): List<AddMembersToWorkspaceMutation.AddMembersToWorkspace>
        = api.addMembersToWorkspace(workspaceId, userIds, role)

    suspend fun addMembersToProject(projectId: Uuid, userIds: List<Uuid>): List<AddMembersToProjectMutation.AddMembersToProject>
        = api.addMembersToProject(projectId, userIds)

    suspend fun createTask(task: Task, creatorId: Uuid? = null): Optional<Task>
        = taskRepo.createTask(task, creatorId)

    suspend fun getTagsByWorkspace(workspaceId: Uuid, ): List<Tag>
        = tagRepo.getTagsByWorkspace(workspaceId = workspaceId)

    suspend fun createTag(tag: Tag): Optional<Tag>
        = tagRepo.createTag(tag)

    suspend fun createProject(userId: Uuid, workspaceId: Uuid, projectName: String): Optional<CreateProjectMutation.CreateProject>
        = api.createProject(userId, workspaceId, projectName)

    suspend fun createWorkspace(userId: Uuid, workspaceName: String): Optional<Workspace> {
        return api.createWorkspace(userId = userId, workspaceName = workspaceName)
    }

    suspend fun getWorkspaceListByUserId(userId: Uuid): List<Workspace> =
        api.getWorkspaceListByUserId(userId)

    suspend fun getMembersByWorkspace(workspaceId: Uuid): List<WorkspaceMember> =
        delegate.getMembersByWorkspace(workspaceId)

    suspend fun getProjectsByWorkspace(workspaceId: Uuid): List<Project>  =
        delegate.getProjectsByWorkspace(workspaceId)

    suspend fun getWorkspaceByWorkspaceId(workspaceId: Uuid): Optional<Workspace> =
        api.getWorkspaceById(workspaceId)

    suspend fun getWorkspaceActivity(workspaceId: Uuid, page: Int? = null, size: Int? = null): WorkspaceActivityPage =
        api.getWorkspaceActivity(workspaceId, page, size)

    // =========================== __PROJECT_DETAIL__ ==================================
    suspend fun getProjectByProjectId(projectId: Uuid): Optional<GetProjectQuery.GetProject> = api.getProjectByProjectId(projectId)
    
    suspend fun getMemberByProject(projectId: Uuid): List<ProjectMember> = api.getMemberByProject(projectId)
}