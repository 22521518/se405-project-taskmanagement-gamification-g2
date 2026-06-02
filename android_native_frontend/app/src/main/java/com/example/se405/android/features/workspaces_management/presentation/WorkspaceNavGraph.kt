@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.se405.android.core.navigations.UuidTypeMap
import com.example.se405.android.features.workspaces_management.presentation.screen.ProjectDetailRoute
import com.example.se405.android.features.workspaces_management.presentation.screen.WorkspaceDetailRoute
import com.example.se405.android.features.workspaces_management.presentation.screen.WorkspaceManagementRoute
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.ProjectDetailViewModel
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceDetailViewModel
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceManagementViewModel
import com.example.se405.android.features.tasks_management.presentation.TaskDetailNav
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun NavGraphBuilder.workspaceNavGraph(navController: NavHostController) {
    navigation<WorkspaceGraphNav>(startDestination = WorkspaceHomeNav) {
        composable<WorkspaceHomeNav> { _ ->
            val viewModel = koinViewModel<WorkspaceManagementViewModel>()
            WorkspaceManagementRoute(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToWorkspace = { id -> navController.navigate(WorkspaceDetailNav(id)) }
            )
        }

        composable<WorkspaceDetailNav>(
            typeMap = UuidTypeMap
        ) { _ ->
            val viewModel = koinViewModel<WorkspaceDetailViewModel>()
            WorkspaceDetailRoute(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToProject = { id -> navController.navigate(ProjectNav(id)) }
            )
        }

        composable<ProjectNav>(
            typeMap = UuidTypeMap
        ) { _ ->
            val viewModel = koinViewModel<ProjectDetailViewModel>()
            ProjectDetailRoute(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToTask = { id -> navController.navigate(TaskDetailNav(id)) }
            )
        }
    }
}

@Serializable
data class WorkspaceDetailNav(val workspaceId: Uuid)

@Serializable
object WorkspaceHomeNav

@Serializable
object WorkspaceGraphNav

@Serializable
data class ProjectNav(val projectId: Uuid)
