@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android_native_frontend.R
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
        indentDp = 4,
        branchContent = { node, _, expanded ->
            val rotation by animateFloatAsState(
                targetValue = if (!expanded) 180f else 0f,
                label = "icon rotation"
            )
            val iconWorkspace = if (expanded) "▼" else "▶"

            when (val data = node.data) {
                is WorkspaceTreeData.WorkspaceData ->
                    Row (modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Text(
                            text = "$iconWorkspace ${data.workspace.name}",
                            style = AppText.DisplayBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "${data.workspace.projects.size} projects",
                            style = AppText.Body2SemiBold,
                            color = BlueGrey80,
                        )
                    }
                is WorkspaceTreeData.ProjectData ->
                    Row (modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = data.project.name,
                                style = AppText.BodyBold,
                                color = MaterialTheme.colorScheme.outline,)

                        Row(modifier = Modifier.padding(start = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                               horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = data.project.tasks.size.toString(),
                                style = AppText.CaptionRegular,
                                color = BlueGrey80,
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.icon_arrow_down),
                                contentDescription = null,
                                modifier = Modifier.rotate(rotation),
                                tint = BlueGrey80
                            )
                        }
                    }
                else -> {}
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


@Preview(showBackground = true)
@Composable
fun TaskGroupPreview() {
    val habits = Workspace(
        id = Uuid.random(),
        name = "Habits",
        projects = listOf(
            Project(
                id = Uuid.random(),
                name = "Personal Habits",
                tasks = PreviewTaskData.tasks
            )
        )
    )

    val tree = remember(PreviewWPData.workspaces, habits) {
        (PreviewWPData.workspaces + habits).map { it.toTree() }
    }

    Android_native_frontendTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TaskGroup(
                roots = tree,
                modifier = Modifier.weight(1f),
                onTaskClick = { task -> println("Clicked: ${task.title}") }
            )
        }
    }
}