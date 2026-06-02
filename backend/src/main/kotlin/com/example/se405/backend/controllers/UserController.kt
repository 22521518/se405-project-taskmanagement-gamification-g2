package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.repository.UserRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class UserController(
    private val userRepository: UserRepository,
) {
    @QueryMapping
    fun getUsersExcluding(@Argument excludeUserId: UUID): List<UserEntity> {
        return userRepository.findByUuidNot(excludeUserId)
    }
}