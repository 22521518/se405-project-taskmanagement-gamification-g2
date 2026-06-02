@file:OptIn(ExperimentalUuidApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.se405.android.features.tasks_management.presentation.screen

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.presentation.components.CalendarHeader
import com.example.se405.android.features.tasks_management.presentation.components.CreateTagPopUp
import com.example.se405.android.features.tasks_management.presentation.components.PersonalAllTasksSection
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailActionUiState
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailCreatePopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailEditPopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailPopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskGroup
import com.example.se405.android.features.tasks_management.presentation.components.toDomainTask
import com.example.se405.android.features.tasks_management.presentation.components.toEpochMillisAtStartOfDay
import com.example.se405.android.features.tasks_management.presentation.components.toTree
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskManagementViewModel
import com.example.se405.android.features.workspaces_management.presentation.components.SubScreenHeader
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
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    var activeTab by remember { mutableStateOf(TaskTab.DATE) }
    var selectedStatus by remember { mutableStateOf<TaskStatus?>(null) }

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
                else -> { /* Other events can be handled if needed */ }
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

    val handleTaskClick: (Task) -> Unit = { task ->
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
                            mutableStateOf(
                                TaskDetailActionUiState(
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
                                )
                            )
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
                            onRepetitionChange = { editUiState = editUiState.copy(repetition = it) },
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxSize()) {
            SubScreenHeader {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.clickable { onSettingsClick() }) {
                        Icon(
                            painter = painterResource(R.drawable.icon_person),
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = "Tasks",
                        style = AppText.HeadBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            TaskTabRow(activeTab = activeTab, onTabSelected = { activeTab = it })

            when (activeTab) {
                TaskTab.ALL -> PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh(viaPull = true) },
                    modifier = Modifier.weight(1f)
                ) {
                    PersonalAllTasksSection(
                        tasks = tasks,
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = it },
                        onTaskClick = handleTaskClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                TaskTab.DATE -> {
                    CalendarHeader(
                        focusDate = selectedDate,
                        onAddTask = null,
                        onDateClick = { viewModel.getTaskByDate(it) }
                    )

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refresh(viaPull = true) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(modifier),
                                selectedDate = localDate,
                                onTaskClick = handleTaskClick
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val taskDraft = viewModel.createTaskDraft()
                popupController.push { onDismiss ->
                    var uiState by remember {
                        mutableStateOf(
                            TaskDetailActionUiState(
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
                            )
                        )
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
                                membersInWorkspace = members,
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
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_plus),
                contentDescription = "Add Task",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

enum class TaskTab { DATE, ALL }

@Composable
private fun TaskTabRow(
    activeTab: TaskTab,
    onTabSelected: (TaskTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(TaskTab.DATE to "By date", TaskTab.ALL to "All")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        tabs.forEach { (tab, title) ->
            TaskTabItem(
                modifier = Modifier.weight(1f),
                title = title,
                isActive = activeTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun TaskTabItem(
    modifier: Modifier = Modifier,
    title: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        Color.Gray.copy(alpha = 0.6f)
    }
    val textStyle = if (isActive) AppText.BodyBold else AppText.BodySemiBold

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = textColor, style = textStyle)
    }
}