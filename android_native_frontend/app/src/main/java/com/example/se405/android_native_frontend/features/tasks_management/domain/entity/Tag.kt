package com.example.se405.android_native_frontend.features.tasks_management.domain.entity

import com.example.se405.android_native_frontend.features.users_management.domain.entity.User
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Tag @OptIn(ExperimentalUuidApi::class)
constructor(
    val createdBy: Uuid,
    val taskIds: List<Uuid>,

    val uuid: Uuid,
    val name: String,
    val color: Int,

    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,

    val creator: User?,
    val tasks: List<Task>,
    )