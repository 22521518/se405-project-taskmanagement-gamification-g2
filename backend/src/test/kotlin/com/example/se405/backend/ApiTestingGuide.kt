package com.example.se405.backend

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@DisplayName("API Testing Guide for SE405 Backend")
class ApiTestingGuide {

    @Test
    @DisplayName("Guide: Test REST APIs with cURL")
    fun testRestApisWithCurl() {
        println("""
            ╔════════════════════════════════════════════════════════════════╗
            ║         REST API Testing with cURL                            ║
            ╚════════════════════════════════════════════════════════════════╝
            
            1. Test Hello Endpoint:
               curl -X GET http://localhost:8080/hello
            
            2. Register User:
               curl -X POST http://localhost:8080/api/auth/register \
                 -H "Content-Type: application/json" \
                 -d '{
                   "email": "user@test.com",
                   "username": "testuser",
                   "password": "password123",
                   "displayName": "Test User"
                 }'
            
            3. Login User:
               curl -X POST http://localhost:8080/api/auth/login \
                 -H "Content-Type: application/json" \
                 -d '{
                   "username": "testuser",
                   "password": "password123"
                 }'
            
            4. Enable Biometric (requires token from login):
               curl -X POST http://localhost:8080/api/auth/enable-biometric \
                 -H "Authorization: Bearer {TOKEN}" \
                 -H "Content-Type: application/json" \
                 -d '{
                   "deviceId": "device123",
                   "publicKey": "your-public-key"
                 }'
        """.trimIndent())
    }

    @Test
    @DisplayName("Guide: Test GraphQL APIs")
    fun testGraphQLApis() {
        println("""
            ╔════════════════════════════════════════════════════════════════╗
            ║         GraphQL API Testing                                   ║
            ╚════════════════════════════════════════════════════════════════╝
            
            Access GraphQL Playground: http://localhost:8080/graphql
            
            1. Create Task:
               mutation CreateTask {
                 createTask(input: {
                   title: "My Task"
                   description: "Task description"
                   type: TASK
                   status: TODO
                   priority: HIGH
                   startDate: "2026-05-20"
                   dueDate: "2026-05-27"
                   creatorId: "550e8400-e29b-41d4-a716-446655440000"
                 }) {
                   uuid
                   title
                   status
                 }
               }
            
            2. Get Tasks by User:
               query GetTasks {
                 getTasks(userId: "550e8400-e29b-41d4-a716-446655440000") {
                   uuid
                   title
                   description
                   status
                   priority
                   creator {
                     email
                     displayName
                   }
                   tags {
                     uuid
                     name
                   }
                 }
               }
            
            3. Get Task Completion Logs:
               query GetLogs {
                 getTaskCompletionLogs(taskId: "task-uuid") {
                   uuid
                   completedAt
                   user {
                     email
                     displayName
                   }
                 }
               }
        """.trimIndent())
    }

    @Test
    @DisplayName("Guide: Run Tests with Gradle")
    fun runTestsWithGradle() {
        println("""
            ╔════════════════════════════════════════════════════════════════╗
            ║         Running Tests with Gradle                             ║
            ╚════════════════════════════════════════════════════════════════╝
            
            1. Run all tests:
               cd backend
               ./gradlew test
            
            2. Run specific test class:
               ./gradlew test --tests AuthControllerSimpleTests
            
            3. Run specific test method:
               ./gradlew test --tests "AuthControllerSimpleTests.should*"
            
            4. Run with detailed output:
               ./gradlew test --info
            
            5. Run tests with JUnit report:
               ./gradlew test
               # Report: build/reports/tests/test/index.html
        """.trimIndent())
    }

    @Test
    @DisplayName("Guide: Test with Postman Collection")
    fun testWithPostman() {
        println("""
            ╔════════════════════════════════════════════════════════════════╗
            ║         Using Postman for API Testing                         ║
            ╚════════════════════════════════════════════════════════════════╝
            
            1. Create a new Collection: "SE405 API Tests"
            
            2. Create requests:
               - GET /hello
               - POST /api/auth/register
               - POST /api/auth/login
               - POST /api/auth/enable-biometric
            
            3. Set Variables:
               - {{base_url}} = http://localhost:8080
               - {{token}} = (saved from login response)
            
            4. Use Pre-request Scripts to extract token:
               var jsonData = pm.response.json();
               pm.environment.set("token", jsonData.token);
            
            5. Save collection for reuse and sharing
        """.trimIndent())
    }

    @Test
    @DisplayName("Guide: Quick .http file testing (VS Code)")
    fun testWithHttpFile() {
        println("""
            ╔════════════════════════════════════════════════════════════════╗
            ║         REST Client Extension (VS Code)                       ║
            ╚════════════════════════════════════════════════════════════════╝
            
            Create file: test-api.http
            
            @baseUrl = http://localhost:8080
            @contentType = application/json
            
            ### Test Hello
            GET {{baseUrl}}/hello
            
            ### Register User
            POST {{baseUrl}}/api/auth/register
            Content-Type: {{contentType}}
            
            {
              "email": "user@test.com",
              "username": "testuser",
              "password": "password123",
              "displayName": "Test User"
            }
            
            ### Login User
            POST {{baseUrl}}/api/auth/login
            Content-Type: {{contentType}}
            
            {
              "username": "testuser",
              "password": "password123"
            }
            
            Then click "Send Request" above each endpoint to test.
        """.trimIndent())
    }
}
