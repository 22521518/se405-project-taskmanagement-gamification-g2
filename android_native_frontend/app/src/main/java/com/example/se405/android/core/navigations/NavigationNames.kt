package com.example.se405.android.core.navigations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.se405.android.features.workspaces_management.presentation.WorkspaceHomeNav
import kotlinx.serialization.Serializable

/**
 * Main dashboard screen route for the Tasks Management epic.
 * This screen doesn't require any arguments.
 */
@Serializable
object PersonalNav

@Serializable
data class EditProfileNav(
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null
)

// Cấu trúc dữ liệu cho từng Tab
data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: Any
)

val bottomNavItems = listOf(
    BottomNavItem("Task", Icons.Rounded.Home, TaskManagementNav),
    BottomNavItem("Workspace", Icons.Rounded.Folder, WorkspaceHomeNav),
    BottomNavItem("Message", Icons.Rounded.ChatBubbleOutline, ConversationListNav),
    BottomNavItem("Profile", Icons.Rounded.PersonOutline, PersonalNav)
)

@Serializable
object TaskManagementNav

@Serializable
object BiometricAuthNav

@Serializable
object AuthSettingsNav

@Serializable
object DeviceAuthSuccessNav

@Serializable data object ConversationListNav
@Serializable data class TaskChatNav(val taskId: String, val taskName: String, val avatarUrl: String? = null, val isFromTask : Boolean = true, val pendingParticipantIds: String? = null)
@Serializable data object NewMessageNav
@Serializable data object SearchNav