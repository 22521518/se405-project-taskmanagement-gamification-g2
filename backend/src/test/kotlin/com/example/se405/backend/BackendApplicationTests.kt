package com.example.se405.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import org.springframework.context.annotation.Import

@ActiveProfiles("test")
@SpringBootTest
@Import(TestBackendApplication::class)
class BackendApplicationTests {

	@Test
	fun contextLoads() {
	}

}
