package com.example.se405.android.core.navigations

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.example.se405.android.features.users_management.presentation.screen.EditProfileScreen
import com.example.se405.android.features.users_management.presentation.screen.PersonalScreen
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel

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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isBottomNavVisible = currentDestination?.hierarchy?.any { destination ->
        destination.route?.contains("TaskManagementNav") == true ||
                destination.route?.contains("ConversationListNav") == true ||
                destination.route?.contains("PersonalNav") == true
    } == true

    Scaffold(
        bottomBar = {
            if (isBottomNavVisible) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route?.contains(item.route::class.simpleName ?: "") == true
                        } == true

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Tránh xếp chồng nhiều màn hình khi bấm qua lại liên tục
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(innerPadding),
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
                                taskId = conversationId, // Truyền ID phòng chat thật
                                taskName = conversationName,
                                isFromTask = false // Đánh dấu không phải đi từ Task
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
                // Lấy ViewModel của NewMessage để gọi API
                val viewModel: com.example.se405.android.features.chat_management.presentation.viewmodel.NewMessageViewModel =
                    koinViewModel()

                NewMessageScreen(
                    viewModel = viewModel, // Chuyền ViewModel vào Screen
                    onClose = {
                        navController.popBackStack()
                    },
                    onNext = { selectedUserIds ->
                        // Kích hoạt hàm gọi API tạo phòng chat
                        viewModel.createChatAndNavigate(
                            selectedUserIds = selectedUserIds,
                            onSuccess = { realConversationId ->
                                navController.navigate(
                                    TaskChatNav(
                                        taskId = realConversationId,
                                        taskName = if (selectedUserIds.size > 1) "Nhóm chat mới" else "Chat 1:1",
                                        isFromTask = false
                                    )
                                ) {
                                    popUpTo(NewMessageNav) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                )
            }

            composable<TaskChatNav> { navBackStackEntry ->

                val args = navBackStackEntry.toRoute<TaskChatNav>()

                TaskChatScreen(
                    taskId = args.taskId,
                    taskName = args.taskName,
                    isFromTask = args.isFromTask, // Bắt buộc truyền cờ này vào Screen
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
                    // Ở màn hình Task, ID truyền vào là Task ID
                    navigateToChat = { taskId, taskName ->
                        navController.navigate(
                            TaskChatNav(
                                taskId = taskId,
                                taskName = taskName,
                                isFromTask = true // Cờ báo hiệu cần qua trạm trung chuyển
                            )
                        )
                    },
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
                )
            }
            composable<PersonalNav> {
                PersonalScreen(
                    onLogoutClick = {
                        navController.navigate(BiometricAuthNav) {
                            popUpTo(TaskManagementNav) { inclusive = true }
                        }
                    },
                    onEditClick = { displayName, email, avatarUrl ->
                        navController.navigate(
                            EditProfileNav(
                                displayName = displayName,
                                email = email,
                                avatarUrl = avatarUrl
                            )
                        )
                    }
                )
            }

            composable<EditProfileNav> { navBackStackEntry ->
                val args = navBackStackEntry.toRoute<EditProfileNav>()
                EditProfileScreen(
                    currentName = args.displayName,
                    currentEmail = args.email,
                    currentAvatarUrl = args.avatarUrl,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}