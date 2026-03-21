package com.example.se405.android_native_frontend.features.tasks_management.domain.entity

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class HabitLabels @OptIn( ExperimentalUuidApi::class)
constructor(
    val labelId: Uuid,
    val labelName: String,
    val description: String,
    val icon: String,

    val habitIds: List<Uuid>,
    val habits: List<Task>,
)
