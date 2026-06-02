package com.example.se405.backend.services

import com.example.se405.backend.database.repository.UserRepository
import jakarta.transaction.Transactional
import java.util.UUID

class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun updatePresence(userId: UUID, isOnline: Boolean): Boolean {
        val user = userRepository.findById(userId).orElseThrow { Exception("User not found") }
        user.isOnline = isOnline
        if (!isOnline) {
            user.lastSeen = java.time.LocalDateTime.now()
        }
        userRepository.save(user)
        return true
    }
}