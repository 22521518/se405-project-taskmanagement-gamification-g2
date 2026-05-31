package com.example.se405.android.features.users_management.domain.repository
import com.example.se405.android.features.users_management.domain.entity.User

interface UserRepository {
    suspend fun getAllUsers(): List<User>
    suspend fun updateFcmToken(token: String): Result<Boolean>
    suspend fun updateUserPresence(isOnline: Boolean): Result<Boolean>
}