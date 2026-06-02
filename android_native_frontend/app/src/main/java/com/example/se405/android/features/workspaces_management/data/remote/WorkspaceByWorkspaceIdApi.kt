@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.data.remote

import com.example.se405.android.features.tasks_management.domain.entity.ProjectMember
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
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

interface CreateGetWorkspaceByWorkspaceIdApi {
    suspend fun getUsersExcluding(userId: Uuid): List<GetUsersExcluding>
    suspend fun addMembersToWorkspace(workspaceId: Uuid, userIds: List<Uuid>, role: WorkspaceRole): List<AddMembersToWorkspaceMutation.AddMembersToWorkspace>

    suspend fun addMembersToProject(projectId: Uuid, userIds: List<Uuid>): List<AddMembersToProjectMutation.AddMembersToProject>
    suspend fun addMemberToWorkspace(workspaceId: Uuid, userId: Uuid, role: WorkspaceRole): Optional<AddMemberToWorkspaceMutation.AddMemberToWorkspace>
    suspend fun addMemberToProject(projectId: Uuid, userId: Uuid): Optional<AddMemberToProjectMutation.AddMemberToProject>
    suspend fun createProject(userId: Uuid, workspaceId: Uuid, projectName: String): Optional<CreateProjectMutation.CreateProject>
    suspend fun createWorkspace(userId: Uuid, workspaceName: String): Optional<Workspace>
    suspend fun getWorkspaceListByUserId(userId: Uuid): List<Workspace>
    suspend fun getMemberByProject(projectId: Uuid): List<ProjectMember>
    suspend fun getWorkspaceById(workspaceId: Uuid): Optional<Workspace>
    suspend fun getWorkspaceActivity(workspaceId: Uuid, page: Int? = null, size: Int? = null): WorkspaceActivityPage

    suspend fun getProjectByProjectId(projectId: Uuid): Optional<GetProjectQuery.GetProject>
}


data class PageInfo(
    val totalElements: Int,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

data class WorkspaceActivityPage(
    val content: List<TaskCompletionLog>,
    val pageInfo: PageInfo,
)