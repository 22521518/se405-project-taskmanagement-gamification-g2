@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.domain.entity

import com.example.se405.android_native_frontend.core.presentation.components.BuiltinLabels
import com.example.se405.android_native_frontend.features.users_management.domain.entity.User
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
            avatarUrl = "",
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
        dueDate = LocalDate.parse("2025-01-20"),
    )

    private val habitTaskBase = Task(
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
            taskId = habitTaskBase.uuid,
            date = LocalDate.parse("2025-01-06"),
            completedAt = LocalDateTime.parse("2025-01-06T20:00:00"),
            task = habitTaskBase,
            user = users[1],
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
            id = Uuid.random(),
            name = "Android Native Frontend",
            tasks = listOf(tasks[0]),
        ),
        Project(
            id = Uuid.random(),
            name = "Personal Development",
            tasks = listOf(tasks[1]),
        ),
    )

    val workspaces = listOf(
        Workspace(
            id = workspaceId,
            name = "SE405 Workspace",
            projects = projects,
            members = workspaceMembers,
        )
    )
}