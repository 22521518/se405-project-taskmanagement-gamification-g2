@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.data.remote

import com.apollographql.apollo.ApolloClient
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceRole
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.graphql.GetMembersByWorkspaceQuery
import com.example.se405.android.graphql.GetMembersByWorkspaceQuery.User as GqlUser
import com.example.se405.android.graphql.GetProjectsByWorkspaceQuery
import com.example.se405.android.graphql.type.WorkspaceRole as GqlWorkspaceRole
import java.time.LocalDateTime
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class WorkspaceApiImpl(private val apolloClient: ApolloClient) : WorkspaceApi {
    override suspend fun getProjectsByWorkspace(workspaceId: Uuid): Optional<List<Project>> {
        val response = apolloClient.query(GetProjectsByWorkspaceQuery(workspaceId.toString())).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val projects = response.data?.getProjectsByWorkspace ?: return Optional.empty()
        return Optional.of(projects.map { project ->
            Project(
                id = Uuid.parse(project.uuid),
                name = project.name,
                tasks = emptyList(),
            )
        })
    }

    override suspend fun getMembersByWorkspace(workspaceId: Uuid): Optional<List<WorkspaceMember>> {
        val response = apolloClient.query(GetMembersByWorkspaceQuery(workspaceId.toString())).execute()
        if (!response.errors.isNullOrEmpty()) {
            return Optional.empty()
        }

        val members = response.data?.getMembersByWorkspace ?: return Optional.empty()
        return Optional.of(members.map { member ->
            WorkspaceMember(
                workspaceId = member.workspaceId.let(Uuid::parse),
                userId = member.userId.let(Uuid::parse),
                role = member.role.toWorkspaceRole(),
                joinedAt = member.joinedAt.toLocalDateTimeOrNow(),
                user = member.user?.toUser(),
            )
        })
    }

    private fun GqlUser.toUser(): User {
        return User(
            uuid = this.uuid.let(Uuid::parse),
            username = this.username,
            email = this.email,
            displayName = this.displayName,
            avatarUrl = this.avatarUrl,
            createdAt = null,
            updatedAt = null,
            passwordHash = null,
        )
    }

    private fun GqlWorkspaceRole.toWorkspaceRole(): WorkspaceRole =
        when (this) {
            GqlWorkspaceRole.OWNER -> WorkspaceRole.OWNER
            GqlWorkspaceRole.MEMBER -> WorkspaceRole.MEMBER
            else -> WorkspaceRole.MEMBER
        }
}


fun String?.toLocalDateTimeOrNow(): LocalDateTime {
    if (this.isNullOrBlank()) return LocalDateTime.now()
    return runCatching { LocalDateTime.parse(this) }.getOrDefault(LocalDateTime.now())
}