@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.BlueGrey80
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.Workspace
import java.util.Calendar
import kotlin.collections.plus
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
    selectedDate: java.time.LocalDate = java.time.LocalDate.now()
) {
    Column(modifier = modifier.fillMaxSize()) {
        TreeList(
            roots = roots,
            modifier = Modifier.weight(1f),
            indentDp = 4,
            branchContent = { node, _, expanded ->
                when (val data = node.data) {
                    is WorkspaceTreeData.WorkspaceData ->
                        Row (modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically
                        ){
                            val rotation by animateFloatAsState(
                                targetValue = if (!expanded) 180f else 0f,
                                label = "icon rotation"
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.icon_arrow_down),
                                contentDescription = null,
                                modifier = Modifier.rotate(rotation),
                                tint = Color.Black
                            )
                            Column {
                                Text(
                                    text = data.workspace.name,
                                    style = AppText.HeadBold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${data.workspace.projects.size} projects",
                                    style = AppText.CaptionSemiBold,
                                    color = Color.Gray
                                )
                            }
                        }
                    is WorkspaceTreeData.ProjectData ->
                        Row (modifier = Modifier
                            .fillMaxWidth().padding(vertical = 4.dp, horizontal = 36.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
                                Text(text = data.project.name,
                                    style = AppText.BodyBold,
                                    color = MaterialTheme.colorScheme.onPrimary,)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${data.project.tasks.size}",
                                        style = AppText.Body2Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
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
                            },
                            selectedDate = selectedDate
                        )
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
            )))

    val tree = remember(PreviewDomainEntityData.workspaces, habits) {
        (PreviewDomainEntityData.workspaces + habits).map { it.toTree() }
    }
    val projects = PreviewDomainEntityData.projects
    val availableTags = PreviewDomainEntityData.tags
    val members = PreviewDomainEntityData.workspaceMembers

    val date = Calendar.getInstance()
    date.add(Calendar.DATE, -1)
    val mockPopupController = remember { PopupController() }
    Android_Theme {
        CompositionLocalProvider(LocalPopupController provides mockPopupController) {
            val popupController = LocalPopupController.current
            Column(modifier = Modifier.fillMaxSize()) {
                CalendarHeader(focusDate = Calendar.getInstance(), onAddTask = {
                    val baseTask = PreviewDomainEntityData.tasks[0]
                    popupController.push { onDismiss ->
                        var uiState by remember {
                            mutableStateOf(TaskDetailActionUiState(
                                title = "",
                                taskType = baseTask.type,
                                selectedTags = emptyList(),
                                priority = baseTask.priority,
                                description = "",
                                selectedStartDateMillis = null,
                                selectedDueDateMillis = null,
                                isTagSelectorVisible = false,
                                selectedProjectId = null,
                                selectedAssigneeId = null,
                                repetition = 1
                            ))
                        }

                        TaskDetailCreatePopUp(
                            taskDraft = baseTask,
                            uiState = uiState,
                            availableTags = availableTags,
                            projectsInWorkspace = projects,
                            membersInWorkspace = members,
                            canChangeTaskType = true,
                            onTaskTypeChange = { uiState = uiState.copy(taskType = it) },
                            onTitleChange = { uiState = uiState.copy(title = it) },
                            onDescriptionChange = { uiState = uiState.copy(description = it) },
                            onPriorityChange = { uiState = uiState.copy(priority = it) },
                            onToggleTag = { tag ->
                                val newTags = if (uiState.selectedTags.contains(tag)) {
                                    uiState.selectedTags.filter { t -> t.uuid != tag.uuid }
                                } else {
                                    uiState.selectedTags + tag
                                }
                                uiState = uiState.copy(selectedTags = newTags)
                            },
                            onProjectSelected = { uiState = uiState.copy(selectedProjectId = it) },
                            onAssigneeSelected = { uiState = uiState.copy(selectedAssigneeId = it) },
                            onTagSelectorVisibilityChange = { uiState = uiState.copy(isTagSelectorVisible = it) },
                            onDone = {
                                // call viewmodel to save
                                onDismiss()
                            },
                            onCancel = { onDismiss() },
                            onRepetitionChange = { repetition -> uiState = uiState.copy(repetition = repetition) }
                        )
                    }
                })
                TaskGroup(
                    roots = tree,
                    modifier = Modifier.weight(1f),
                    onTaskClick = { task -> println("Clicked: ${task.title}") }
                )
            }
        }
    }
}