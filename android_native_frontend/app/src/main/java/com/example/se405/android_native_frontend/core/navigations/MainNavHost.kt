package com.example.se405.android_native_frontend.core.navigations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.se405.android_native_frontend.core.presentation.components.ButtonApp
import com.example.se405.android_native_frontend.core.presentation.components.ButtonType
import com.example.se405.android_native_frontend.core.utils.PreviewTaskData
import com.example.se405.android_native_frontend.features.tasks_management.presentation.components.TaskCard
import com.example.se405.android_native_frontend.navigation.ScreenANav
import com.example.se405.android_native_frontend.navigation.ScreenBNav

/**
 * Defines navigation route models for the application using
 * the official Jetpack Compose Navigation library.
 *
 * This file demonstrates a type-safe navigation approach where
 * each destination is represented as a serializable Kotlin type
 * instead of a raw string route.
 *
 * All destinations must:
 * - Be annotated with @Serializable
 * - Contain only serializable argument types
 *
 * The Compose Navigation type-safe API will automatically:
 * - Serialize arguments into route format
 * - Deserialize them from the NavBackStackEntry
 */

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ScreenANav,
        modifier = modifier,
    ) {
        composable<ScreenANav> { ScreenA(navController) }
        composable<ScreenBNav> {
            val args = it.toRoute<ScreenBNav>()
            ScreenB(navController, args.arg1, args.arg2)
        }
    }
}


@Composable
fun ScreenA(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PreviewTaskData.tasks.forEach { TaskCard(it, {})  }
        ButtonApp (onClick = { navController.navigate(ScreenBNav("John", 25)) }) {
            Text(text = "Go to Screen B")
        }
    }
}

@Composable
fun ScreenB(navController: NavHostController, name: String?, age: Int = 10) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ButtonApp(onClick = { navController.navigate(ScreenANav) }, type = ButtonType.OUTLINED) {
            Text(text = "Hello {$name} - {$age}, move to Screen A")
        }
    }
}

