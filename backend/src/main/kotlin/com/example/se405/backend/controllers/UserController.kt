package com.example.se405.backend.controllers

import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.repository.UserRepository
import com.example.se405.backend.services.UserService
import com.example.se405.backend.utils.getCurrentUserUuid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Controller
class UserController(
    private val userRepository: UserRepository,
    private val userService: UserService
) {
    @QueryMapping
    fun getUsersExcluding(@Argument excludeUserId: UUID): List<UserEntity> {
        return userRepository.findByUuidNot(excludeUserId)
    @MutationMapping
    fun updateFcmToken(@Argument token: String): Boolean {
        val auth = SecurityContextHolder.getContext().authentication

        if (auth == null || auth.name == "anonymousUser") {
            return false
        }

        val user = userRepository.findByUsername(auth.name) ?: return false

        user.fcmToken = token
        userRepository.save(user)

        return true
    }

    @MutationMapping
    fun updateUserPresence(@Argument isOnline: Boolean): Boolean {
        val principal = SecurityContextHolder.getContext().authentication.principal
        val userId = principal as? UUID ?: getCurrentUserUuid()

        return userService.updatePresence(userId, isOnline)
    }
}