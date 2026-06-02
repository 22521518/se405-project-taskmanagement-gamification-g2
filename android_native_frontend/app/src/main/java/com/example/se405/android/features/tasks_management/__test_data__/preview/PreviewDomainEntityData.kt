@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.__test_data__.preview

import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.ProjectMember
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceRole
import com.example.se405.android.features.users_management.domain.entity.User
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object PreviewDomainEntityData {
    val users = listOf(
        User(
            uuid = Uuid.random(),
            email = "owner@example.com",
            username = "owner_user",
            passwordHash = "hash_owner",
            displayName = "Workspace Owner",
            avatarUrl = null,
            createdAt = LocalDateTime.parse("2025-01-01T08:00:00"),
            updatedAt = LocalDateTime.parse("2025-01-01T08:00:00"),
        ),
        User(
            uuid = Uuid.random(),
            email = "member@example.com",
            username = "member_user",
            passwordHash = "hash_member",
            displayName = "Workspace Member",
            avatarUrl = "",
            createdAt = LocalDateTime.parse("2025-01-02T09:00:00"),
            updatedAt = LocalDateTime.parse("2025-01-02T09:00:00"),
        ),
    )

    private val workspaceId = Uuid.random()
    private val projectId1 = Uuid.random()
    val projectMembers = listOf(
        ProjectMember(
            projectId = projectId1,
            workspaceId = workspaceId,
            userId = users.first().uuid,
            joinedAt = LocalDateTime.parse("2025-01-05T12:00:00"),
            user = users.first()
        )
    )

    val workspaceMembers = listOf(
        WorkspaceMember(
            workspaceId = workspaceId,
            userId = users[0].uuid,
            role = WorkspaceRole.OWNER,
            joinedAt = LocalDateTime.parse("2025-01-03T10:00:00"),
            user = users[0],
        ),
        WorkspaceMember(
            workspaceId = workspaceId,
            userId = users[1].uuid,
            role = WorkspaceRole.MEMBER,
            joinedAt = LocalDateTime.parse("2025-01-03T11:00:00"),
            user = users[1],
        ),
    )

    private val projectTaskBase = Task(
        uuid = Uuid.random(),
        title = "Implement workspace API",
        description = "Implement project and member endpoints Implement project and member endpoints Implement project and member endpoints Implement project and member endpoints Implement project and member endpoints Implement project and member endpoints Implement project and member endpoints Implement project and member endpoints Implement project and member endpoints Implement project and member endpoints Implement project and member endpoints Implement project and member endpoints ",
        repetition = 0,
        type = TaskType.PROJECT,
        status = TaskStatus.IN_PROGRESS,
        priority = TaskPriority.HIGH,
        creator = users[0],
        tags = emptyList(),
        taskCompletionLog = emptyList(),
        startDate = LocalDate.parse("2025-01-05"),
        dueDate = LocalDate.parse("2027-01-20"),
    )

     val habitTaskBase = Task(
        uuid = Uuid.random(),
        title = "Daily code review",
        description = "Review code at least 30 minutes",
        repetition = 1,
        type = TaskType.HABIT,
        status = TaskStatus.TODO,
        priority = TaskPriority.MEDIUM,
        creator = users[1],
        tags = emptyList(),
        taskCompletionLog = emptyList(),
        startDate = LocalDate.parse("2025-01-06"),
        dueDate = LocalDate.parse("2025-01-06"),
    )

    val taskCompletionLogs = listOf(
        TaskCompletionLog(
            taskCompletionId = Uuid.random(),
            date = LocalDate.parse("2025-01-06"),
            status = TaskStatus.DONE,
            completedAt = LocalDateTime.parse("2025-01-06T20:00:00"),
            taskId = habitTaskBase.uuid,
            userId = users[1].uuid,
            taskTitle = habitTaskBase.title,
            userDisplayName = users[1].displayName
        ),
        TaskCompletionLog(
            taskCompletionId = Uuid.random(),
            date = LocalDate.parse("2025-01-06"),
            status = TaskStatus.DONE,
            completedAt = LocalDateTime.parse("2025-01-06T20:00:00"),
            taskId = habitTaskBase.uuid,
            userId = users[1].uuid,
            taskTitle = habitTaskBase.title,
            userDisplayName = users[1].displayName
        ),
        TaskCompletionLog(
            taskCompletionId = Uuid.random(),
            date = LocalDate.parse("2025-01-06"),
            status = TaskStatus.DONE,
            completedAt = LocalDateTime.parse("2025-01-06T20:00:00"),
            taskId = habitTaskBase.uuid,
            userId = users[1].uuid,
            taskTitle = habitTaskBase.title,
            userDisplayName = users[1].displayName
        )
    )

    val tags = listOf(
        Tag(
            createdBy = users[0].uuid,
            ownershipType = TagOwnershipType.WORKSPACE,
            workspaceId = workspaceId,
            taskIds = listOf(projectTaskBase.uuid),
            uuid = Uuid.random(),
            name = "Backend",
            color = 0xFF1E88E5.toInt(),
            label = BuiltinLabels[0],
            createdAt = LocalDateTime.parse("2025-01-04T09:00:00"),
            updatedAt = LocalDateTime.parse("2025-01-04T09:00:00"),
            creator = users[0],
            tasks = listOf(projectTaskBase),
        ),
        Tag(
            createdBy = users[1].uuid,
            ownershipType = TagOwnershipType.PERSONAL,
            workspaceId = null,
            taskIds = listOf(habitTaskBase.uuid),
            uuid = Uuid.random(),
            name = "Routine",
            color = 0xFF43A047.toInt(),
            label = BuiltinLabels[2],
            createdAt = LocalDateTime.parse("2025-01-04T10:00:00"),
            updatedAt = LocalDateTime.parse("2025-01-04T10:00:00"),
            creator = users[1],
            tasks = listOf(habitTaskBase),
        ),
    )

    val tasks = listOf(
        projectTaskBase.copy(tags = listOf(tags[0])),
        habitTaskBase.copy(
            tags = listOf(tags[1]),
            taskCompletionLog = taskCompletionLogs,
        ),
    )
    val projects = listOf(
        Project(
            id = projectId1,
            workspaceId = workspaceId,
            name = "Android Native Frontend",
            tasks = listOf(tasks[0], tasks[0].copy(status = TaskStatus.DONE), tasks[0].copy(status = TaskStatus.FAILED)),
            member = listOf(projectMembers[0], projectMembers[0].copy(user = projectMembers[0].user?.copy(avatarUrl = "https://images.unsplash.com/photo-1780054694213-869dbdee8c9c?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxmZWF0dXJlZC1waG90b3MtZmVlZHw5fHx8ZW58MHx8fHx8")), projectMembers[0], projectMembers[0]),
        ),
        Project(
            id = Uuid.random(),
            workspaceId = workspaceId,
            name = "Personal Development",
            tasks = listOf(tasks[1], tasks[0].copy(uuid = Uuid.random())),
        ),
    )

    val workspaces = listOf(
        Workspace(
            id = Uuid.parse("00009914-0000-0000-0000-000000000001"),
            name = "SE405 Workspace",
            projects = projects,
            members = workspaceMembers,
        ),
        Workspace(
            id = Uuid.parse("00009914-1234-0000-0000-000000000001"),
            name = "Habitt Workspace",
            projects = projects,
            members = workspaceMembers,
        )
    )
}