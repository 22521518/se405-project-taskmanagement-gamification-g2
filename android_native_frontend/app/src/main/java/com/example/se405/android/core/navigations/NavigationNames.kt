package com.example.se405.android.core.navigations

import kotlinx.serialization.Serializable

/**
 * Main dashboard screen route for the Tasks Management epic.
 * This screen doesn't require any arguments.
 */
@Serializable
object TaskManagementNav

@Serializable
object BiometricAuthNav

@Serializable
data class TaskChatNav(
    val taskId: String,
    val taskName: String
)

@Serializable
object AuthSettingsNav

@Serializable
object DeviceAuthSuccessNav