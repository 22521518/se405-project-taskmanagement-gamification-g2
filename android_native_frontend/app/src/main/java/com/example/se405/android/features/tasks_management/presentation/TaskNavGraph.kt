@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.se405.android.core.navigations.UuidTypeMap
import com.example.se405.android.features.tasks_management.presentation.screen.TaskDetailRoute
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskDetailViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun NavGraphBuilder.taskNavGraph(navController: NavHostController) {
    navigation<TaskGraphNav>(startDestination = TaskDetailNav::class) {
        composable<TaskDetailNav>(
            typeMap = UuidTypeMap
        ) { _ ->
            val viewModel = koinViewModel<TaskDetailViewModel>()
            TaskDetailRoute(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Serializable
object TaskGraphNav

@Serializable
data class TaskDetailNav(val taskId: Uuid)
