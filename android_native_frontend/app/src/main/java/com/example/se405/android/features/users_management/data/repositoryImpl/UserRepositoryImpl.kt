package com.example.se405.android.features.users_management.data.repositoryImpl

import com.apollographql.apollo.ApolloClient
import com.example.se405.android.features.users_management.domain.repository.UserRepository
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.graphql.GetAllUsersQuery
import com.example.se405.android.graphql.UpdateFcmTokenMutation
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UserRepositoryImpl(
    private val apolloClient: ApolloClient
) : UserRepository {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getAllUsers(): List<User> {
        val response = apolloClient.query(GetAllUsersQuery()).execute()

        // Bắt lỗi nếu API trả về lỗi
        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Lỗi không xác định từ GraphQL")
        }

        // Map dữ liệu từ GraphQL DTO sang Domain Model của bạn
        return response.data?.getAllUsers?.map { dto ->
            val now = java.time.LocalDateTime.now()
            User(
                uuid = Uuid.parse(dto.uuid),
                username = dto.username,
                email = dto.email,
                displayName = dto.displayName,
                avatarUrl = dto.avatarUrl ?: "",
                passwordHash = null,
                createdAt = now,
                updatedAt = now
            )
        } ?: emptyList()
    }

    override suspend fun updateFcmToken(token: String): Result<Boolean> {
        return try {
            val response = apolloClient.mutation(UpdateFcmTokenMutation(token)).execute()

            if (response.hasErrors()) {
                val errorMsg = response.errors?.firstOrNull()?.message ?: "Lỗi từ Backend khi cập nhật Token"
                Result.failure(Exception(errorMsg))
            } else {
                // Trả về true hoặc false dựa vào kết quả từ GraphQL
                val success = response.data?.updateFcmToken ?: false
                Result.success(success)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}