package com.example.se405.android.features.chat_management.domain.entity

data class Conversation(
    val uuid: String,
    val type: String, // DIRECT, GROUP, TASK
    val name: String?,
    val taskUuid: String?
)