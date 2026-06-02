@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.features.tasks_management.presentation.components.CalendarHeader
import com.example.se405.android.features.tasks_management.presentation.components.CreateTagPopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailActionUiState
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailCreatePopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailEditPopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailPopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskGroup
import com.example.se405.android.features.tasks_management.presentation.components.toDomainTask
import com.example.se405.android.features.tasks_management.presentation.components.toEpochMillisAtStartOfDay
import com.example.se405.android.features.tasks_management.presentation.components.toTree
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskManagementViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun TaskManagementScreen(
    onSettingsClick: () -> Unit,
    navigateToChat: (taskId: String, taskName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskManagementViewModel = koinViewModel()
) {
    val workspaces by viewModel.workspaces.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val members by viewModel.members.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val popupController = LocalPopupController.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _ -> }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> { }
            }
        }
    }

    val treeRoots = remember(workspaces) {
        workspaces.map { it.toTree() }
    }

    val localDate = remember(selectedDate) {
        java.time.LocalDate.of(
            selectedDate.get(java.util.Calendar.YEAR),
            selectedDate.get(java.util.Calendar.MONTH) + 1,
            selectedDate.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        CalendarHeader(
            focusDate = selectedDate,
            onAddTask = {
                val taskDraft = viewModel.createTaskDraft()
                popupController.push { onDismiss ->
                    var uiState by remember {
                        mutableStateOf(TaskDetailActionUiState(
                            title = "",
                            taskType = taskDraft.type,
                            selectedTags = emptyList(),
                            priority = taskDraft.priority,
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
                        taskDraft = taskDraft,
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
                            val newTags = if (uiState.selectedTags.any { it.uuid == tag.uuid }) {
                                uiState.selectedTags.filter { t -> t.uuid != tag.uuid }
                            } else {
                                uiState.selectedTags + tag
                            }
                            uiState = uiState.copy(selectedTags = newTags)
                        },
                        onProjectSelected = { uiState = uiState.copy(selectedProjectId = it) },
                        onAssigneeSelected = { uiState = uiState.copy(selectedAssigneeId = it) },
                        onTagSelectorVisibilityChange = { uiState = uiState.copy(isTagSelectorVisible = it) },
                        onCreateTagClick = {
                            popupController.push { onDismissTag ->
                                CreateTagPopUp(
                                    onCreate = { tagState ->
                                        viewModel.createTag(tagState) { createdTag ->
                                            if (createdTag != null) {
                                                uiState = uiState.copy(
                                                    selectedTags = uiState.selectedTags + createdTag
                                                )
                                                onDismissTag()
                                            }
                                        }
                                    },
                                    onCancel = { onDismissTag() },
                                    isLoading = isLoading
                                )
                            }
                        },
                        onEditTagClick = { tagToEdit ->
                            popupController.push { onDismissTagEdit ->
                                CreateTagPopUp(
                                    initialTag = tagToEdit,
                                    onCreate = {},
                                    onCancel = { onDismissTagEdit() },
                                    onUpdate = { tag, tagState ->
                                        viewModel.updateTag(tag, tagState) { updatedTag ->
                                            if (updatedTag != null) {
                                                uiState = uiState.copy(
                                                    selectedTags = uiState.selectedTags.map {
                                                        if (it.uuid == updatedTag.uuid) updatedTag else it
                                                    }
                                                )
                                                onDismissTagEdit()
                                            }
                                        }
                                    },
                                    isLoading = isLoading
                                )
                            }
                        },
                        onDeleteTagClick = { tagToDelete ->
                            viewModel.deleteTag(tagToDelete.uuid) { success ->
                                if (success) {
                                    uiState = uiState.copy(
                                        selectedTags = uiState.selectedTags.filterNot { it.uuid == tagToDelete.uuid }
                                    )
                                }
                            }
                        },
                        onDone = {
                            val createdTask = uiState.toDomainTask(
                                baseTask = taskDraft,
                                status = TaskStatus.TODO,
                                uuid = Uuid.random(),
                            )
                            viewModel.createTask(createdTask) { success ->
                                if (success) {
                                    onDismiss()
                                }
                            }
                        },
                        onCancel = { onDismiss() },
                        isLoading = isLoading,
                        onOpenDateRangePicker = {
                            popupController.push { onDismissDatePicker ->
                                com.example.se405.android.core.presentation.components.DateRangePickerPopUp(
                                    initialStartDate = uiState.selectedStartDateMillis,
                                    initialEndDate = uiState.selectedDueDateMillis,
                                    onDateRangeSelected = { (start, end) ->
                                        uiState = uiState.copy(
                                            selectedStartDateMillis = start,
                                            selectedDueDateMillis = end
                                        )
                                    },
                                    onDismiss = onDismissDatePicker
                                )
                            }
                        },
                        onRepetitionChange = { repetition -> uiState = uiState.copy(repetition = repetition) }
                    )
                }
            },
            onDateClick = { viewModel.getTaskByDate(it) }
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (isLoading && treeRoots.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            } else if (error != null && treeRoots.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_cross_box),
                        contentDescription = "Error Icon",
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = "Something went wrong",
                        style = AppText.HeadBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = error ?: "Unknown error occurred",
                        style = AppText.BodyRegular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.size(24.dp))
                    ButtonApp(
                        onClick = { viewModel.refresh() },
                        type = ButtonType.FILLED
                    ) {
                        Text("Retry", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            } else if (treeRoots.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_plus),
                        contentDescription = "Empty State Icon",
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = "No Tasks Found",
                        style = AppText.HeadBold,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Get started by creating your first task!",
                        style = AppText.BodyRegular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                TaskGroup(
                    roots = treeRoots,
                    modifier = Modifier.fillMaxSize().then(modifier),
                    selectedDate = localDate,
                    onTaskClick = { task ->
                        popupController.push { onDismiss ->
                            TaskDetailPopUp(
                                task = task,
                                onChatClick = {
                                    onDismiss()
                                    navigateToChat(task.uuid.toString(), task.title)
                                },
                                onEditClick = { taskToEdit ->
                                    popupController.push { onDismissEdit ->
                                         var editUiState by remember {
                                             mutableStateOf(TaskDetailActionUiState(
                                                 title = taskToEdit.title,
                                                 taskType = taskToEdit.type,
                                                 selectedTags = taskToEdit.tags,
                                                 priority = taskToEdit.priority,
                                                 description = taskToEdit.description,
                                                 selectedStartDateMillis = taskToEdit.startDate?.toEpochMillisAtStartOfDay(),
                                                 selectedDueDateMillis = taskToEdit.dueDate?.toEpochMillisAtStartOfDay(),
                                                 isTagSelectorVisible = false,
                                                 selectedProjectId = taskToEdit.projectId,
                                                 selectedAssigneeId = taskToEdit.assignee?.uuid,
                                                 repetition = taskToEdit.repetition
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
                                             onRepetitionChange = {  editUiState = editUiState.copy(repetition = it) },
                                             onToggleTag = { tag ->
                                                 val newTags = if (editUiState.selectedTags.any { it.uuid == tag.uuid }) {
                                                     editUiState.selectedTags.filter { t -> t.uuid != tag.uuid }
                                                 } else {
                                                     editUiState.selectedTags + tag
                                                 }
                                                 editUiState = editUiState.copy(selectedTags = newTags)
                                             },
                                             onProjectSelected = { editUiState = editUiState.copy(selectedProjectId = it) },
                                             onAssigneeSelected = { editUiState = editUiState.copy(selectedAssigneeId = it) },
                                             onTagSelectorVisibilityChange = { editUiState = editUiState.copy(isTagSelectorVisible = it) },
                                             onOpenDateRangePicker = {
                                                 popupController.push { onDismissDatePicker ->
                                                     com.example.se405.android.core.presentation.components.DateRangePickerPopUp(
                                                         initialStartDate = editUiState.selectedStartDateMillis,
                                                         initialEndDate = editUiState.selectedDueDateMillis,
                                                         onDateRangeSelected = { (start, end) ->
                                                             editUiState = editUiState.copy(
                                                                 selectedStartDateMillis = start,
                                                                 selectedDueDateMillis = end
                                                             )
                                                         },
                                                         onDismiss = onDismissDatePicker
                                                     )
                                                 }
                                             },
                                             onCreateTagClick = {
                                                 popupController.push { onDismissTag ->
                                                     CreateTagPopUp(
                                                         onCreate = { tagState ->
                                                             viewModel.createTag(tagState) { createdTag ->
                                                                 if (createdTag != null) {
                                                                     editUiState = editUiState.copy(
                                                                         selectedTags = editUiState.selectedTags + createdTag
                                                                     )
                                                                     onDismissTag()
                                                                 }
                                                             }
                                                         },
                                                         onCancel = { onDismissTag() },
                                                         isLoading = isLoading
                                                     )
                                                 }
                                             },
                                             onEditTagClick = { tagToEdit ->
                                                 popupController.push { onDismissTagEdit ->
                                                     CreateTagPopUp(
                                                         initialTag = tagToEdit,
                                                         onCreate = {},
                                                         onCancel = { onDismissTagEdit() },
                                                         onUpdate = { tag, tagState ->
                                                             viewModel.updateTag(tag, tagState) { updatedTag ->
                                                                 if (updatedTag != null) {
                                                                     editUiState = editUiState.copy(
                                                                         selectedTags = editUiState.selectedTags.map {
                                                                             if (it.uuid == updatedTag.uuid) updatedTag else it
                                                                         }
                                                                     )
                                                                     onDismissTagEdit()
                                                                 }
                                                             }
                                                         },
                                                         isLoading = isLoading
                                                     )
                                                 }
                                             },
                                             onDeleteTagClick = { tagToDelete ->
                                                 viewModel.deleteTag(tagToDelete.uuid) { success ->
                                                     if (success) {
                                                         editUiState = editUiState.copy(
                                                             selectedTags = editUiState.selectedTags.filterNot { it.uuid == tagToDelete.uuid }
                                                         )
                                                     }
                                                 }
                                             },
                                             onDone = {
                                                 val updatedTask = editUiState.toDomainTask(
                                                     baseTask = taskToEdit,
                                                     membersInWorkspace = members,
                                                 )
                                                 viewModel.updateTask(updatedTask) { success ->
                                                     if (success) {
                                                         onDismissEdit()
                                                     }
                                                 }
                                             },
                                             onCancel = { onDismissEdit() },
                                             isLoading = isLoading
                                         )
                                     }
                                },
                                onClose = { onDismiss() },
                                onDone = {
                                    viewModel.markTaskDone(task) { success ->
                                        if (success) {
                                            onDismiss()
                                        }
                                    }
                                },
                                onWontDo = {
                                    viewModel.markTaskWontDo(task) { success ->
                                        if (success) {
                                            onDismiss()
                                        }
                                    }
                                },
                                onDelete = {
                                    viewModel.deleteTask(task)
                                    onDismiss()
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}