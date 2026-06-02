package com.example.se405.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester

import org.springframework.context.annotation.Import
import org.springframework.beans.factory.annotation.Autowired
import com.example.se405.backend.database.repository.TaskRepository
import java.util.UUID

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@Import(TestBackendApplication::class)
class SimpleTest {
    @Autowired
    lateinit var taskRepository: TaskRepository

    @Test
    fun contextLoads() {
        val targetUuid = UUID.fromString("6518a752-83cb-499d-86e2-bec0c404c4e0")
        println("=== DB DIAGNOSTICS ===")
        val allTasks = taskRepository.findAll()
        println("All tasks count in DB: ${allTasks.size}")
        allTasks.forEach { task ->
            println("Task: uuid=${task.uuid}, title='${task.title}', type='${task.type}', creatorId=${task.creatorId}, projectId=${task.projectId}")
        }
        
        val queriedTasks = taskRepository.findTaskResponsibilitiesByUserIdWithAssignees(targetUuid)
        println("Queried tasks for user $targetUuid: count=${queriedTasks.size}")
        queriedTasks.forEach { task ->
            println("Queried Task: uuid=${task.uuid}, title='${task.title}'")
        }
        println("=== END DB DIAGNOSTICS ===")
    }
}
