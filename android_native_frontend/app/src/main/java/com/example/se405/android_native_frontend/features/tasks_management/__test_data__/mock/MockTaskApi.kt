package com.example.se405.android_native_frontend.features.tasks_management.__test_data__.mock

import com.example.se405.android_native_frontend.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Project
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Workspace
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.WorkspaceMember
import kotlinx.coroutines.delay

class MockTaskApi {
    suspend fun getWorkspaces(): List<Workspace> {
        delay(500)
        return PreviewDomainEntityData.workspaces
    }

    suspend fun getAvailableTags(): List<Tag> {
        delay(100)
        return PreviewDomainEntityData.tags
    }

    suspend fun getProjectsInWorkspace(): List<Project> {
        delay(100)
        return PreviewDomainEntityData.projects
    }

    suspend fun getMembersInWorkspace(): List<WorkspaceMember> {
        delay(100)
        return PreviewDomainEntityData.workspaceMembers
    }
}
