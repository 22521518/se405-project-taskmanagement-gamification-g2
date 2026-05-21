package com.example.se405.android.core.navigations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.ui.AuthSettingsScreen
import com.example.se405.android.core.authentication.ui.BiometricAuthScreen
import com.example.se405.android.core.authentication.ui.DeviceAuthSuccessScreen
import com.example.se405.android.features.chat_management.presentation.screen.ConversationListScreen
import com.example.se405.android.features.chat_management.presentation.screen.NewMessageScreen
import com.example.se405.android.features.chat_management.presentation.screen.SearchScreen
import com.example.se405.android.features.chat_management.presentation.screen.TaskChatScreen
import com.example.se405.android.features.tasks_management.presentation.screen.TaskManagementScreen
import com.example.se405.android.navigation.AuthSettingsNav
import com.example.se405.android.navigation.BiometricAuthNav
import com.example.se405.android.navigation.DeviceAuthSuccessNav
import com.example.se405.android.navigation.TaskManagementNav
import org.koin.compose.koinInject

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {

    val authPreferences: AuthPreferences = koinInject()

    val authToken by authPreferences.authToken.collectAsState(initial = null)

    val isAuthenticated = !authToken.isNullOrBlank()

    val startDestination =
        if (isAuthenticated) TaskManagementNav
        else BiometricAuthNav

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {

        composable<BiometricAuthNav> {
            BiometricAuthScreen(
                onAuthenticated = {
                    navController.navigate(TaskManagementNav) {
                        popUpTo(BiometricAuthNav) {
                            inclusive = true
                        }
                    }
                },
                onGuestAuthenticated = {
                    navController.navigate(TaskManagementNav) {
                        popUpTo(BiometricAuthNav) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<DeviceAuthSuccessNav> {
            DeviceAuthSuccessScreen(
                onLogout = {
                    navController.navigate(BiometricAuthNav) {
                        popUpTo(DeviceAuthSuccessNav) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<ConversationListNav> {
            ConversationListScreen(
                onNavigateToChat = { conversationId, conversationName ->
                    navController.navigate(
                        TaskChatNav(
                            taskId = conversationId,
                            taskName = conversationName
                        )
                    )
                },
                onNavigateToNewMessage = {
                    navController.navigate(NewMessageNav)
                },
                onNavigateToSearch = {
                    navController.navigate(SearchNav)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<SearchNav> {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<NewMessageNav> {
            NewMessageScreen(
                onClose = {
                    navController.popBackStack()
                },
                onNext = { _ ->
                    val newConversationId =
                        java.util.UUID.randomUUID().toString()

                    navController.navigate(
                        TaskChatNav(
                            taskId = newConversationId,
                            taskName = "Chat Mới"
                        )
                    ) {
                        popUpTo(NewMessageNav) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<TaskChatNav> { navBackStackEntry ->

            val args =
                navBackStackEntry.toRoute<TaskChatNav>()

            TaskChatScreen(
                taskId = args.taskId,
                taskName = args.taskName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<TaskManagementNav> {

            if (!isAuthenticated) {
                LaunchedEffect(Unit) {
                    navController.navigate(BiometricAuthNav) {
                        popUpTo(TaskManagementNav) {
                            inclusive = true
                        }
                    }
                }
                return@composable
            }

            TaskManagementScreen(
                onSettingsClick = {
                    navController.navigate(AuthSettingsNav)
                },
                navigateToChat = { taskId, taskName ->
                    navController.navigate(
                        TaskChatNav(
                            taskId = taskId,
                            taskName = taskName
                        )
                    )
                },
                onInboxClick = {
                    navController.navigate(ConversationListNav)
                }
            )
        }

        composable<AuthSettingsNav> {

            if (!isAuthenticated) {
                LaunchedEffect(Unit) {
                    navController.navigate(BiometricAuthNav) {
                        popUpTo(AuthSettingsNav) {
                            inclusive = true
                        }
                    }
                }
                return@composable
            }

            AuthSettingsScreen(
                onGoHome = {
                    navController.navigate(TaskManagementNav) {
                        popUpTo(AuthSettingsNav) {
                            inclusive = true
                        }
                    }
                },
                onLogout = {
                    navController.navigate(BiometricAuthNav) {
                        popUpTo(TaskManagementNav) {
                            inclusive = true
                        }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}