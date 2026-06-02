package com.example.se405.backend

import org.springframework.boot.SpringApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import com.example.se405.backend.main as appMain

@TestConfiguration(proxyBeanMethods = false)
class TestBackendApplication {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageName.parse("postgres:17.9"))
            .withEnv("TZ", "UTC")

}

fun main(args: Array<String>) {
    SpringApplication.from(::appMain)
        .with(TestBackendApplication::class.java)
        .run(*args)
}
