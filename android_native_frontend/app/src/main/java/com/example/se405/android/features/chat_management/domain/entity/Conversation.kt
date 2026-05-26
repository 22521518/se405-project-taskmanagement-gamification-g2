package com.example.se405.android.features.chat_management.domain.entity

import com.example.se405.android.features.users_management.domain.entity.User

data class Conversation(
    val uuid: String,
    val type: String,
    val name: String?,
    val taskUuid: String?,
    val participants: List<User> = emptyList()
)