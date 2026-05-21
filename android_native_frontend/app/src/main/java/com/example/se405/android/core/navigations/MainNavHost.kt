package com.example.se405.android.core.navigations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.ui.AuthSettingsScreen
import com.example.se405.android.core.authentication.ui.BiometricAuthScreen
import com.example.se405.android.core.authentication.ui.DeviceAuthSuccessScreen
import com.example.se405.android.features.tasks_management.presentation.screen.TaskManagementScreen
import com.example.se405.android.navigation.AuthSettingsNav
import com.example.se405.android.navigation.BiometricAuthNav
import com.example.se405.android.navigation.DeviceAuthSuccessNav
import com.example.se405.android.navigation.TaskManagementNav
import org.koin.compose.koinInject

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
    val authPreferences: AuthPreferences = koinInject()
    val authToken by authPreferences.authToken.collectAsState(initial = null)
    val isAuthenticated = !authToken.isNullOrBlank()
    val startDestination = if (isAuthenticated) TaskManagementNav else BiometricAuthNav

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<BiometricAuthNav> {
            BiometricAuthScreen(
                onAuthenticated = {
                    navController.navigate(AuthSettingsNav) {
                        popUpTo(BiometricAuthNav) { inclusive = true }
                    }
                },
                onGuestAuthenticated = {
                    navController.navigate(DeviceAuthSuccessNav) {
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
        
        composable<TaskManagementNav> {
            if (!isAuthenticated) {
                LaunchedEffect(Unit) {
                    navController.navigate(BiometricAuthNav) {
                        popUpTo(TaskManagementNav) { inclusive = true }
                    }
                }
                return@composable
            }

            TaskManagementScreen(
                onSettingsClick = {
                    navController.navigate(AuthSettingsNav)
                }
            )
        }
        
        composable<AuthSettingsNav> {
            if (!isAuthenticated) {
                LaunchedEffect(Unit) {
                    navController.navigate(BiometricAuthNav) {
                        popUpTo(AuthSettingsNav) { inclusive = true }
                    }
                }
                return@composable
            }

            AuthSettingsScreen(
                onGoHome = {
                    navController.navigate(TaskManagementNav) {
                        popUpTo(AuthSettingsNav) { inclusive = true }
                    }
                },
                onLogout = {
                    navController.navigate(BiometricAuthNav) {
                        popUpTo(TaskManagementNav) { inclusive = true }
                    }
                }
            )
        }
    }
}
