package com.example.se405.backend.controllers

import com.example.se405.backend.TestBackendApplication
import com.example.se405.backend.database.model.WorkspaceMemberEntity
import com.example.se405.backend.database.model.WorkspaceMemberId
import com.example.se405.backend.database.model.WorkspaceRole
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@Import(TestBackendApplication::class)
@Transactional
class WorkspaceControllerTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var graphQlTester: HttpGraphQlTester

    // ── Query Tests ───────────────────────────────────────────────────────────

    @Test
    fun `getWorkspace returns workspace when exists`() {
        val user = createUser()
        val workspace = createWorkspace(owner = user)

        graphQlTester.document("""
            query {
                getWorkspace(uuid: "${workspace.uuid}") {
                    uuid
                    name
                }
            }
        """).execute()
            .path("getWorkspace.uuid").entity(String::class.java).isEqualTo(workspace.uuid.toString())
            .path("getWorkspace.name").entity(String::class.java).isEqualTo("Test WS")
    }

    @Test
    fun `getWorkspace returns null when not found`() {
        val randomUuid = UUID.randomUUID()

        graphQlTester.document("""
            query {
                getWorkspace(uuid: "$randomUuid") {
                    uuid
                }
            }
        """).execute()
            .path("getWorkspace").valueIsNull()
    }

    @Test
    fun `getProjectsByWorkspace returns projects for workspace`() {
        val user = createUser()
        val workspace = createWorkspace(owner = user)
        val project1 = createProject("Project A", workspace)
        val project2 = createProject("Project B", workspace)

        graphQlTester.document("""
            query {
                getProjectsByWorkspace(workspaceId: "${workspace.uuid}") {
                    uuid
                    name
                }
            }
        """).execute()
            .path("getProjectsByWorkspace").entityList(Map::class.java).hasSize(2)
    }

    @Test
    fun `getProjectsByWorkspace returns empty list when workspace has no projects`() {
        val user = createUser()
        val workspace = createWorkspace(owner = user)

        graphQlTester.document("""
            query {
                getProjectsByWorkspace(workspaceId: "${workspace.uuid}") {
                    uuid
                }
            }
        """).execute()
            .path("getProjectsByWorkspace").entityList(Map::class.java).hasSize(0)
    }

    // ── SchemaMapping Tests ───────────────────────────────────────────────────

    @Test
    fun `workspace projects field resolves correctly`() {
        val user = createUser()
        val workspace = createWorkspace(owner = user)
        createProject("P1", workspace)
        createProject("P2", workspace)

        graphQlTester.document("""
            query {
                getWorkspace(uuid: "${workspace.uuid}") {
                    projects {
                        name
                    }
                }
            }
        """).execute()
            .path("getWorkspace.projects").entityList(Map::class.java).hasSize(2)
    }

    @Test
    fun `workspace members field resolves correctly`() {
        val user = createUser()
        val workspace = createWorkspace(owner = user)

        graphQlTester.document("""
            query {
                getWorkspace(uuid: "${workspace.uuid}") {
                    members {
                        user { uuid }
                    }
                }
            }
        """).execute()
            .path("getWorkspace.members").entityList(Map::class.java).hasSize(1)
    }

    // ── Mutation Tests ────────────────────────────────────────────────────────

    @Test
    fun `createWorkspace saves and returns new workspace`() {
        val user = createUser()

        graphQlTester.document("""
            mutation {
                createWorkspace(input: { name: "New WS", ownerId: "${user.uuid}" }) {
                    uuid
                    name
                }
            }
        """).execute()
            .path("createWorkspace.name").entity(String::class.java).isEqualTo("New WS")
            .path("createWorkspace.uuid").hasValue()

        // Verify actually persisted
        val all = workspaceRepository.findAll()
        assert(all.any { it.name == "New WS" })
    }

    @Test
    fun `createProject saves and returns new project`() {
        val user = createUser()
        val workspace = createWorkspace(owner = user)

        graphQlTester.document("""
            mutation {
                createProject(input: { name: "New Project", workspaceId: "${workspace.uuid}" }) {
                    uuid
                    name
                }
            }
        """).execute()
            .path("createProject.name").entity(String::class.java).isEqualTo("New Project")
    }

    @Test
    fun `deleteWorkspace removes workspace and returns it`() {
        val user = createUser()
        val workspace = createWorkspace(owner = user)

        graphQlTester.document("""
            mutation {
                deleteWorkspace(uuid: "${workspace.uuid}") {
                    uuid
                    name
                }
            }
        """).execute()
            .path("deleteWorkspace.uuid").entity(String::class.java).isEqualTo(workspace.uuid.toString())

        assert(workspaceRepository.findById(workspace.uuid!!).isEmpty)
    }

    @Test
    fun `deleteWorkspace throws when not found`() {
        val randomUuid = UUID.randomUUID()

        graphQlTester.document("""
            mutation {
                deleteWorkspace(uuid: "$randomUuid") {
                    uuid
                }
            }
        """).execute()
            .errors().satisfy { errors ->
                assert(errors.isNotEmpty())
                assert(errors.any { error ->
                    error.extensions["classification"] == "INTERNAL_ERROR"
                            || error.message?.contains("Workspace not found") == true
                })
            }
    }

    @Test
    fun `deleteProject removes project and returns it`() {
        val user = createUser()
        val workspace = createWorkspace(owner = user)
        val project = createProject(workspace = workspace)

        graphQlTester.document("""
            mutation {
                deleteProject(uuid: "${project.uuid}") {
                    uuid
                    name
                }
            }
        """).execute()
            .path("deleteProject.uuid").entity(String::class.java).isEqualTo(project.uuid.toString())

        assert(projectRepository.findById(project.uuid!!).isEmpty)
    }
}