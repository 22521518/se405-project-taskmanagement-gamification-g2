package com.example.se405.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester

import org.springframework.context.annotation.Import

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@Import(TestBackendApplication::class)
class SimpleTest {
    @Test
    fun contextLoads() {}
}
