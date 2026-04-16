package com.example.se405.android.core.navigations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.se405.android.features.tasks_management.presentation.screen.TaskManagementScreen
import com.example.se405.android.navigation.TaskManagementNav

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
        startDestination = TaskManagementNav,
        modifier = modifier,
    ) {
        composable<TaskManagementNav> { 
            TaskManagementScreen() 
        }
    }
}
