package com.example.se405.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.WebGraphQlTester
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureMockMvc(addFilters = false)
@Import(TestBackendApplication::class)
@WithMockUser
class BackendIntegrationTests {

    @Autowired
    private lateinit var graphQlTester: WebGraphQlTester

    @Test
    fun `should create a task via GraphQL`() {
        val createMutation = """
            mutation {
                createTask(input: {
                    title: "Test Task"
                    type: HABIT
                    status: TODO
                    priority: HIGH
                    startDate: "2026-04-14"
                    dueDate: "2026-04-20"
                }) {
                    uuid
                    title
                    type
                }
            }
        """.trimIndent()

        graphQlTester.document(createMutation)
            .execute()
            .path("createTask.title").entity(String::class.java).isEqualTo("Test Task")
            .path("createTask.type").entity(String::class.java).isEqualTo("HABIT")
    }
}
