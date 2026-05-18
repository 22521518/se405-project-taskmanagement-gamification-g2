@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)

package com.example.se405.android.features.tasks_management.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailActionUiState
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailCreatePopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailEditPopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailPopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskGroup
import com.example.se405.android.features.tasks_management.presentation.components.toTree
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskManagementViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun TaskManagementScreen(
    onSettingsClick: () -> Unit,
    navigateToChat: (taskId: String, taskName: String) -> Unit,
    onInboxClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TaskManagementViewModel = koinViewModel()
) {
    val workspaces by viewModel.workspaces.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val members by viewModel.members.collectAsState()

    val popupController = LocalPopupController.current

    val treeRoots = remember(workspaces) {
        workspaces.map { it.toTree() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Không gian làm việc",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onInboxClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChatBubbleOutline,
                    contentDescription = "Tin nhắn"
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        TaskGroup(
            roots = treeRoots,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onAddTaskClick = {
//                val baseTask = PreviewDomainEntityData.tasks.firstOrNull() ?: return@TaskGroup
//
//                popupController.push { onDismiss ->
//                    var uiState by remember {
//                        mutableStateOf(TaskDetailActionUiState(
//                            title = "",
//                            taskType = baseTask.type,
//                            selectedTags = emptyList(),
//                            priority = baseTask.priority,
//                            description = "",
//                            selectedStartDateMillis = null,
//                            selectedDueDateMillis = null,
//                            isTagSelectorVisible = false,
//                            selectedProjectId = null,
//                            selectedAssigneeId = null
//                        ))
//                    }
//
//                    TaskDetailCreatePopUp(
//                        taskDraft = baseTask,
//                        uiState = uiState,
//                        availableTags = availableTags,
//                        projectsInWorkspace = projects,
//                        membersInWorkspace = members,
//                        canChangeTaskType = true,
//                        onTaskTypeChange = { uiState = uiState.copy(taskType = it) },
//                        onTitleChange = { uiState = uiState.copy(title = it) },
//                        onDescriptionChange = { uiState = uiState.copy(description = it) },
//                        onPriorityChange = { uiState = uiState.copy(priority = it) },
//                        onToggleTag = { tag ->
//                            val newTags = if (uiState.selectedTags.contains(tag)) {
//                                uiState.selectedTags.filter { t -> t.uuid != tag.uuid }
//                            } else {
//                                uiState.selectedTags + tag
//                            }
//                            uiState = uiState.copy(selectedTags = newTags)
//                        },
//                        onProjectSelected = { uiState = uiState.copy(selectedProjectId = it) },
//                        onAssigneeSelected = { uiState = uiState.copy(selectedAssigneeId = it) },
//                        onTagSelectorVisibilityChange = { uiState = uiState.copy(isTagSelectorVisible = it) },
//                        onDone = {
//                            onDismiss()
//                        },
//                        onCancel = { onDismiss() }
//                    )
//                }
            },
            onTaskClick = { task ->
                popupController.push { onDismiss ->
                    TaskDetailPopUp(
                        task = task,
                        onEditClick = { taskToEdit ->
                            popupController.push { onDismissEdit ->
                                var editUiState by remember {
                                    mutableStateOf(TaskDetailActionUiState(
                                        title = taskToEdit.title,
                                        taskType = taskToEdit.type,
                                        selectedTags = taskToEdit.tags,
                                        priority = taskToEdit.priority,
                                        description = taskToEdit.description,
                                        selectedStartDateMillis = null,
                                        selectedDueDateMillis = null,
                                        isTagSelectorVisible = false,
                                        selectedProjectId = null,
                                        selectedAssigneeId = null
                                    ))
                                }

                                TaskDetailEditPopUp(
                                    task = taskToEdit,
                                    uiState = editUiState,
                                    availableTags = availableTags,
                                    projectsInWorkspace = projects,
                                    membersInWorkspace = members,
                                    onTitleChange = { editUiState = editUiState.copy(title = it) },
                                    onDescriptionChange = { editUiState = editUiState.copy(description = it) },
                                    onPriorityChange = { editUiState = editUiState.copy(priority = it) },
                                    onToggleTag = { tag ->
                                        val newTags = if (editUiState.selectedTags.contains(tag)) {
                                            editUiState.selectedTags.filter { t -> t.uuid != tag.uuid }
                                        } else {
                                            editUiState.selectedTags + tag
                                        }
                                        editUiState = editUiState.copy(selectedTags = newTags)
                                    },
                                    onProjectSelected = { editUiState = editUiState.copy(selectedProjectId = it) },
                                    onAssigneeSelected = { editUiState = editUiState.copy(selectedAssigneeId = it) },
                                    onTagSelectorVisibilityChange = { editUiState = editUiState.copy(isTagSelectorVisible = it) },
                                    onDone = {
                                        onDismissEdit()
                                    },
                                    onCancel = { onDismissEdit() }
                                )
                            }
                        },
                        onChatClick = { taskToChat ->
                            onDismiss()
                            navigateToChat(taskToChat.uuid.toString(), taskToChat.title)
                        },
                        onClose = { onDismiss() },
                        onDone = { onDismiss() },
                        onWontDo = { onDismiss() },
                        onDelete = { onDismiss() }
                    )
                }
            }
        )
    }
}