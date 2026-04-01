@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.data.remote

import com.example.se405.android_native_frontend.features.tasks_management.data.dto.GetTaskResponse
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TaskApi {
//    @GET("/task")
    suspend fun getTasks(userId: Uuid): List<GetTaskResponse>
}