@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.BlueGrey80
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.Workspace
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "TaskGroup"

sealed class WorkspaceTreeData {
    data class WorkspaceData(val workspace: Workspace) : WorkspaceTreeData()
    data class ProjectData(val project: Project) : WorkspaceTreeData()
    data class TaskData(val task: Task) : WorkspaceTreeData()
}

/**
 * Hierarchical task group UI using workspace -> project -> task tree nodes.
 *
 * This composable is stateless: callers provide tree data and click callback.
 */
@Composable
fun TaskGroup(
    roots: List<TreeNode<WorkspaceTreeData>>,
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {},
    onAddTaskClick: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), contentAlignment = Alignment.CenterEnd) {
            ButtonApp(
                onClick = onAddTaskClick,
                type = ButtonType.FILLED,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_plus),
                        contentDescription = "Add Task",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Add Task", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        TreeList(
            roots = roots,
            modifier = Modifier.weight(1f),
            indentDp = 4,
        branchContent = { node, _, expanded ->
            when (val data = node.data) {
                is WorkspaceTreeData.WorkspaceData ->
                    Row (modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        val iconWorkspace = if (expanded) "▼" else "▶"

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

                            val rotation by animateFloatAsState(
                                targetValue = if (!expanded) 180f else 0f,
                                label = "icon rotation"
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
}

/**
 * Maps a project domain model into tree node format used by `TreeList`.
 */
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


/**
 * Maps a workspace domain model into tree node format used by `TreeList`.
 */
fun Workspace.toTree(): TreeNode<WorkspaceTreeData> = TreeNode(
    id = id.hashCode(),
    name = name,
    data = WorkspaceTreeData.WorkspaceData(this),
    children = projects.map { it.toTree()}
)


/**
 * Preview for TaskGroup.
 *
 * Usage guide:
 * - In production, build `roots` from ViewModel state (domain models -> tree).
 * - Keep navigation/popups outside this composable via `onTaskClick` callback.
 */
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
                tasks = PreviewDomainEntityData.tasks
            )
        )
    )

    val tree = remember(PreviewDomainEntityData.workspaces, habits) {
        (PreviewDomainEntityData.workspaces + habits).map { it.toTree() }
    }

    Android_Theme {
        Column(modifier = Modifier.fillMaxSize()) {
            TaskGroup(
                roots = tree,
                modifier = Modifier.weight(1f),
                onTaskClick = { task -> println("Clicked: ${task.title}") },
                onAddTaskClick = { println("Clicked: Add Task") }
            )
        }
    }
}