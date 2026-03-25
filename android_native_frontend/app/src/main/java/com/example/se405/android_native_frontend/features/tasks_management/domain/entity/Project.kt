package com.example.se405.android_native_frontend.features.tasks_management.domain.entity

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Project @OptIn(ExperimentalUuidApi::class)
constructor(
    val id: Uuid,
    val name: String,
    val tasks: List<Task>,
)