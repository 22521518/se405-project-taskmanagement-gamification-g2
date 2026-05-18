package com.example.se405.android.core.navigations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.se405.android.core.authentication.ui.AuthSettingsScreen
import com.example.se405.android.core.authentication.ui.BiometricAuthScreen
import com.example.se405.android.core.authentication.ui.DeviceAuthSuccessScreen
import com.example.se405.android.features.tasks_management.presentation.screen.TaskManagementScreen
import com.example.se405.android.core.navigations.TaskChatNav
import com.example.se405.android.features.chat_management.presentation.screen.ConversationListScreen
import com.example.se405.android.features.chat_management.presentation.screen.NewMessageScreen
import com.example.se405.android.features.chat_management.presentation.screen.SearchScreen
import com.example.se405.android.features.chat_management.presentation.screen.TaskChatScreen

/**
 * Defines navigation route models for the application using
 * the official Jetpack Compose Navigation library.
 *
 * This file demonstrates a type-safe navigation approach where
 * each destination is represented as a serializable Kotlin type
 * instead of a raw string route.
 */

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,

) {
    NavHost(
        navController = navController,
        startDestination = BiometricAuthNav,
        modifier = modifier,
    ) {
        composable<BiometricAuthNav> {
            BiometricAuthScreen(
                onAuthenticated = {
                    navController.navigate(TaskManagementNav) {
                    //navController.navigate(AuthSettingsNav) {
                        popUpTo(BiometricAuthNav) { inclusive = true }
                    }
                },
                onGuestAuthenticated = {
                    navController.navigate(TaskManagementNav) {
                    //navController.navigate(DeviceAuthSuccessNav) {
                        popUpTo(BiometricAuthNav) { inclusive = true }
                    }
                }
            )
        }

        composable<DeviceAuthSuccessNav> {
            DeviceAuthSuccessScreen(
                onLogout = {
                    navController.navigate(BiometricAuthNav) {
                        popUpTo(DeviceAuthSuccessNav) { inclusive = true }
                    }
                }
            )
        }

        composable<ConversationListNav> {
            ConversationListScreen(
                onNavigateToChat = { conversationId, conversationName ->
                    navController.navigate(TaskChatNav(taskId = conversationId, taskName = conversationName))
                },
                onNavigateToNewMessage = {
                    navController.navigate(NewMessageNav)
                },
                onNavigateToSearch = {
                    navController.navigate(SearchNav)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<SearchNav> {
            SearchScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- MÀN HÌNH TẠO CHAT MỚI ---
        composable<NewMessageNav> {
            NewMessageScreen(
                onClose = { navController.popBackStack() },
                onNext = { selectedUserIds ->
                    // Tương lai: Chỗ này sẽ gọi API tạo phòng chat.
                    // Tạm thời, giả lập tạo xong và chuyển sang màn hình chat 1:1
                    // Bật popUpTo(NewMessageNav) để khi back từ màn hình chat sẽ về lại Inbox chứ ko về lại màn hình Tạo mới
                    val newConversationId = "generated-id-${selectedUserIds.first()}"
                    navController.navigate(TaskChatNav(taskId = newConversationId, taskName = "Chat Mới")) {
                        popUpTo(NewMessageNav) { inclusive = true }
                    }
                }
            )
        }

        // --- MÀN HÌNH CHI TIẾT CHAT (TÁI SỬ DỤNG CHO CẢ TASK VÀ GROUP/1:1) ---
        composable<TaskChatNav> { navBackStackEntry ->
            val args = navBackStackEntry.toRoute<TaskChatNav>()
            TaskChatScreen(
                taskId = args.taskId,
                taskName = args.taskName,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<TaskManagementNav> {
            TaskManagementScreen(
                onSettingsClick = {
                    navController.navigate(AuthSettingsNav)
                },
                navigateToChat = { taskId, taskName ->
                    navController.navigate(TaskChatNav(taskId = taskId, taskName = taskName))
                },
                onInboxClick = {
                    navController.navigate(ConversationListNav)
                }
            )
        }

        composable<TaskChatNav> { navBackStackEntry ->
            val args = navBackStackEntry.toRoute<TaskChatNav>()

            TaskChatScreen(
                taskId = args.taskId,
                taskName = args.taskName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable<AuthSettingsNav> {
            AuthSettingsScreen(
                onLogout = {
                    navController.navigate(BiometricAuthNav) {
                        popUpTo(TaskManagementNav) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }

            )
        }
    }
}
