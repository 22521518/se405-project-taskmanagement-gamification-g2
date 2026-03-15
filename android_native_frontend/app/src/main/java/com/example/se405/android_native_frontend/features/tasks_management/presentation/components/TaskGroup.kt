@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme
import com.example.se405.android_native_frontend.core.presentation.theme.AppText
import com.example.se405.android_native_frontend.core.presentation.theme.BlueGrey80
import com.example.se405.android_native_frontend.core.utils.PreviewTaskData
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "TaskGroup"

data class Workspace(val id: Uuid, val name: String, val projects: List<Project>)
data class Project(val id: Uuid, val name: String, val tasks: List<Task>)

sealed class WorkspaceTreeData {
    data class WorkspaceData(val workspace: Workspace) : WorkspaceTreeData()
    data class ProjectData(val project: Project) : WorkspaceTreeData()
    data class TaskData(val task: Task) : WorkspaceTreeData()
}

// Compose use TreeList
@Composable
fun TaskGroup(
    roots: List<TreeNode<WorkspaceTreeData>>,
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {},
) {
    TreeList(
        roots = roots,
        modifier = modifier,
        indentDp = 0,
        branchContent = { node, _, expanded ->
            when (val data = node.data) {
                is WorkspaceTreeData.WorkspaceData ->
                    Text(
                        text = data.workspace.name,
                        style = AppText.DisplayBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                is WorkspaceTreeData.ProjectData ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                        Text(
                            text = data.project.name,
                            style = AppText.BodyBold,
                            color = MaterialTheme.colorScheme.outline,
                        )

                        Text(
                            text = data.project.tasks.size.toString(),
                            style = AppText.CaptionRegular,
                            color = BlueGrey80,
                        )
                    }
                is WorkspaceTreeData.TaskData -> {}   // branch never a leaf
            }
        },
        leafContent = { node, _ ->
            when (val data = node.data) {
                is WorkspaceTreeData.TaskData -> {
                    val task = data.task
                    TaskCard(
                        task = task,
                        onTaskClick = { onTaskClick(task)
                            Log.d(TAG, "Clicked: ${task.title}")
                        })
                }
                else -> {}  // branch never a leaf
            }
        }
    )
}

// ===== Convert domain -> TreeNode =====
fun Project.toTree(): TreeNode<WorkspaceTreeData> = TreeNode(
    id = id.hashCode(),
    name = name,
    data = WorkspaceTreeData.ProjectData(this),
    children = tasks.map { task ->
        TreeNode(
            id = task.uuid.hashCode(),
            name = task.title,
            data = WorkspaceTreeData.TaskData(task),
        )
    }
)


fun Workspace.toTree(): TreeNode<WorkspaceTreeData> = TreeNode(
    id = id.hashCode(),
    name = name,
    data = WorkspaceTreeData.WorkspaceData(this),
    children = projects.map { it.toTree()}
)


@Preview(
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
    showBackground = true,
    apiLevel = 36,
    backgroundColor = 0xFFccccc1
)
@Composable
fun TaskGroupPreview() {
    val habits = listOf(
        Project(
            id = Uuid.random(),
            name = "Personal Habits",
            tasks = PreviewTaskData.tasks
        )
    )

    val tree1 = remember(PreviewWPData.workspaces) { PreviewWPData.workspaces.map { it.toTree() } }
    val tree2 = remember(habits) { habits.map { it.toTree() } }

    Android_native_frontendTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // Workspace tree
            TaskGroup(
                roots = tree1,
                modifier = Modifier.weight(1f),
                onTaskClick = { task -> println("Clicked: ${task.title}") }
            )

            HorizontalDivider()
            // Standalone project tree (habits)
            TaskGroup(
                roots = tree2,
                modifier = Modifier.weight(1f),
                onTaskClick = { task -> println("Clicked: ${task.title}") }
            )
        }
    }
}