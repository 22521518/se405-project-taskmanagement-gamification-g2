package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.ProjectEntity
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.model.WorkspaceEntity
import com.example.se405.backend.database.model.WorkspaceMemberEntity
import com.example.se405.backend.database.model.WorkspaceMemberId
import com.example.se405.backend.database.model.WorkspaceRole
import com.example.se405.backend.database.repository.ProjectRepository
import com.example.se405.backend.database.repository.UserRepository
import com.example.se405.backend.database.repository.WorkspaceMemberRepository
import com.example.se405.backend.database.repository.WorkspaceRepository
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

abstract class BaseIntegrationTest {

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var workspaceRepository: WorkspaceRepository

    @Autowired
    protected lateinit var projectRepository: ProjectRepository

    @Autowired
    protected lateinit var workspaceMemberRepository: WorkspaceMemberRepository

    // Helper tạo user để dùng trong test
    protected fun createUser(username: String = "tester", displayName: String = "Test User", email: String = "test@example.com"): UserEntity {
        return userRepository.save(UserEntity(uuid = UUID.randomUUID(), username = username, displayName = displayName, email = email, passwordHash = "123456"))
    }

    protected fun createWorkspace(name: String = "Test WS", owner: UserEntity): WorkspaceEntity {
        val ws = workspaceRepository.save(WorkspaceEntity(name = name, owner = owner))
        workspaceMemberRepository.save(WorkspaceMemberEntity(
            id = WorkspaceMemberId(workspaceId = ws.uuid!!, userId = owner.uuid!!),
            user = owner,
            workspace = ws,
            role = WorkspaceRole.OWNER
        ))
        return ws
    }

    protected fun createProject(name: String = "Test Project", workspace: WorkspaceEntity): ProjectEntity {
        return projectRepository.save(ProjectEntity(name = name, workspace = workspace))
    }
}