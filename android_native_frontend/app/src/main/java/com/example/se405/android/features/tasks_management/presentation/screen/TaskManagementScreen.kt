@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.screen

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.AppHeader
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.theme.AppText
import androidx.compose.runtime.CompositionLocalProvider
import com.example.se405.android.core.presentation.components.DateRangePickerPopUp
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.validation.TaskFormRules
import com.example.se405.android.features.tasks_management.presentation.components.CalendarHeader
import com.example.se405.android.features.tasks_management.presentation.components.CreateTagPopUp
import com.example.se405.android.features.tasks_management.presentation.components.PersonalAllTasksSection
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailActionUiState
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailCreatePopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailEditPopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskDetailPopUp
import com.example.se405.android.features.tasks_management.presentation.components.TaskGroup
import com.example.se405.android.features.tasks_management.presentation.components.TreeNode
import com.example.se405.android.features.tasks_management.presentation.components.WorkspaceTreeData
import com.example.se405.android.features.tasks_management.presentation.components.toDomainTask
import com.example.se405.android.features.tasks_management.presentation.components.toEpochMillisAtStartOfDay
import com.example.se405.android.features.tasks_management.presentation.components.toTree
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskManagementViewModel
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskUiEvent
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.util.Calendar
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun TaskManagementRoute(
    onSettingsClick: () -> Unit,
    navigateToChat: (taskId: String, taskName: String) -> Unit,
    navigateToTaskDetail: (taskId: Uuid, projectId: Uuid?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: TaskManagementViewModel = koinViewModel()
) {
    val workspaces by viewModel.workspaces.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val members by viewModel.members.collectAsState()
    val workspacesRaw by viewModel.workspacesRaw.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
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
                is TaskUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
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

    // Tách handleTaskClick ra Route vì nó phụ thuộc vào popupController + viewModel
    val handleTaskClick: (Task) -> Unit = { task ->
        popupController.push { onDismiss ->
            TaskDetailPopUp(
                task = task,
                onChatClick = {
                    onDismiss()
                    navigateToChat(task.uuid.toString(), task.title)
                },
                onViewDetailClick = { detailTask ->
                    onDismiss()
                    navigateToTaskDetail(detailTask.uuid, detailTask.projectId)
                },
                onEditClick = { taskToEdit ->
                    popupController.push { onDismissEdit ->
                        var editUiState by remember {
                            val initialWsId = TaskFormRules.workspaceIdForProject(workspacesRaw, taskToEdit.projectId)
                            mutableStateOf(
                                TaskDetailActionUiState(
                                    title = taskToEdit.title,
                                    taskType = taskToEdit.type,
                                    // Validate existing tags against ownership when loading details.
                                    selectedTags = TaskFormRules.sanitizeTags(taskToEdit.tags, taskToEdit.type, initialWsId),
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

                        // Scope tags/assignees to the selected project's workspace (business rules #1 & #2).
                        val editFormWorkspaceId = TaskFormRules.workspaceIdForProject(workspacesRaw, editUiState.selectedProjectId)
                        val editFilteredTags = TaskFormRules.availableTags(editUiState.taskType, editFormWorkspaceId, availableTags)
                        val editFilteredMembers = TaskFormRules.membersForProject(workspacesRaw, editUiState.selectedProjectId)

                        TaskDetailEditPopUp(
                            task = taskToEdit,
                            uiState = editUiState,
                            availableTags = editFilteredTags,
                            projectsInWorkspace = projects,
                            membersInWorkspace = editFilteredMembers,
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
                            onProjectSelected = { projectId ->
                                val wsId = TaskFormRules.workspaceIdForProject(workspacesRaw, projectId)
                                val validMembers = TaskFormRules.membersForProject(workspacesRaw, projectId)
                                editUiState = editUiState.copy(
                                    selectedProjectId = projectId,
                                    selectedTags = TaskFormRules.sanitizeTags(editUiState.selectedTags, editUiState.taskType, wsId),
                                    selectedAssigneeId = editUiState.selectedAssigneeId?.takeIf { id -> validMembers.any { it.userId == id } },
                                )
                            },
                            onAssigneeSelected = { editUiState = editUiState.copy(selectedAssigneeId = it) },
                            onTagSelectorVisibilityChange = { editUiState = editUiState.copy(isTagSelectorVisible = it) },
                            onOpenDateRangePicker = {
                                popupController.push { onDismissDatePicker ->
                                    DateRangePickerPopUp(
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
                                    membersInWorkspace = editFilteredMembers,
                                )
                                viewModel.updateTask(updatedTask) { success ->
                                    if (success) onDismissEdit()
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
                        if (success) onDismiss()
                    }
                },
                onWontDo = {
                    viewModel.markTaskWontDo(task) { success ->
                        if (success) onDismiss()
                    }
                },
                onDelete = {
                    viewModel.deleteTask(task)
                    onDismiss()
                }
            )
        }
    }

    val handleAddTaskClick: () -> Unit = {
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

            // Scope tags/assignees to the selected project's workspace (business rules #1 & #2).
            val createFormWorkspaceId = TaskFormRules.workspaceIdForProject(workspacesRaw, uiState.selectedProjectId)
            val createFilteredTags = TaskFormRules.availableTags(uiState.taskType, createFormWorkspaceId, availableTags)
            val createFilteredMembers = TaskFormRules.membersForProject(workspacesRaw, uiState.selectedProjectId)

            TaskDetailCreatePopUp(
                taskDraft = taskDraft,
                uiState = uiState,
                availableTags = createFilteredTags,
                projectsInWorkspace = projects,
                membersInWorkspace = createFilteredMembers,
                canChangeTaskType = true,
                onTaskTypeChange = { newType ->
                    uiState = if (newType == TaskType.HABIT) {
                        uiState.copy(
                            taskType = newType,
                            selectedProjectId = null,
                            selectedAssigneeId = null,
                            selectedTags = TaskFormRules.sanitizeTags(uiState.selectedTags, TaskType.HABIT, null),
                        )
                    } else {
                        val wsId = TaskFormRules.workspaceIdForProject(workspacesRaw, uiState.selectedProjectId)
                        uiState.copy(
                            taskType = newType,
                            selectedTags = TaskFormRules.sanitizeTags(uiState.selectedTags, TaskType.PROJECT, wsId),
                        )
                    }
                },
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
                onProjectSelected = { projectId ->
                    val wsId = TaskFormRules.workspaceIdForProject(workspacesRaw, projectId)
                    val validMembers = TaskFormRules.membersForProject(workspacesRaw, projectId)
                    uiState = uiState.copy(
                        selectedProjectId = projectId,
                        selectedTags = TaskFormRules.sanitizeTags(uiState.selectedTags, uiState.taskType, wsId),
                        selectedAssigneeId = uiState.selectedAssigneeId?.takeIf { id -> validMembers.any { it.userId == id } },
                    )
                },
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
                        membersInWorkspace = createFilteredMembers,
                    )
                    viewModel.createTask(createdTask) { success ->
                        if (success) onDismiss()
                    }
                },
                onCancel = { onDismiss() },
                isLoading = isLoading,
                onOpenDateRangePicker = {
                    popupController.push { onDismissDatePicker ->
                        DateRangePickerPopUp(
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
    }

    TaskManagementScreen(
        treeRoots = treeRoots,
        tasks = tasks,
        selectedDate = selectedDate,
        localDate = localDate,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        error = error,
        onSettingsClick = onSettingsClick,
        onTaskClick = handleTaskClick,
        onAddTaskClick = handleAddTaskClick,
        onDateClick = { viewModel.getTaskByDate(it) },
        onRefresh = { viewModel.refresh(viaPull = true) },
        onRetry = { viewModel.refresh() },
        modifier = modifier,
    )
}

// ──────────────────────────────────────────────
// Pure-UI screen — không phụ thuộc vào ViewModel
// ──────────────────────────────────────────────

@Composable
fun TaskManagementScreen(
    // Data
    treeRoots: List<TreeNode<WorkspaceTreeData>>,
    tasks: List<Task>,
    selectedDate: Calendar,
    localDate: LocalDate,
    isLoading: Boolean,
    isRefreshing: Boolean,
    error: String?,
    // Callbacks
    onSettingsClick: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onAddTaskClick: () -> Unit,
    onDateClick: (Calendar) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeTab by remember { mutableStateOf(TaskTab.DATE) }
    var selectedStatus by remember { mutableStateOf<TaskStatus?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxSize()) {
            AppHeader(
                title = "Tasks",
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            painter = painterResource(R.drawable.icon_person),
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
            )

            TaskTabRow(activeTab = activeTab, onTabSelected = { activeTab = it })

            when (activeTab) {
                TaskTab.ALL -> PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.weight(1f)
                ) {
                    PersonalAllTasksSection(
                        tasks = tasks,
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = it },
                        onTaskClick = onTaskClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                TaskTab.DATE -> {
                    CalendarHeader(
                        focusDate = selectedDate,
                        onAddTask = null,
                        onDateClick = onDateClick
                    )

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when {
                            isLoading && treeRoots.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                }
                            }

                            error != null && treeRoots.isEmpty() -> {
                                TaskManagementErrorState(
                                    error = error,
                                    onRetry = onRetry
                                )
                            }

                            treeRoots.isEmpty() -> {
                                TaskManagementEmptyState()
                            }

                            else -> {
                                TaskGroup(
                                    roots = treeRoots,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(modifier),
                                    selectedDate = localDate,
                                    onTaskClick = onTaskClick
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddTaskClick,
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

// ──────────────────────────────────────────────
// Private sub-composables
// ──────────────────────────────────────────────

@Composable
private fun TaskManagementErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
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
            text = error,
            style = AppText.BodyRegular,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(24.dp))
        ButtonApp(onClick = onRetry, type = ButtonType.FILLED) {
            Text("Retry", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun TaskManagementEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
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
}

// ──────────────────────────────────────────────
// Tab row (unchanged, kept private)
// ──────────────────────────────────────────────

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
    val textColor = if (isActive) MaterialTheme.colorScheme.onPrimary
    else Color.Gray.copy(alpha = 0.6f)
    val textStyle = if (isActive) AppText.BodyBold else AppText.BodySemiBold

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = textColor, style = textStyle)
    }
}

// ──────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────

/**
 * Helper bọc Android_Theme + PopupController cho mọi preview của screen này.
 * Giống pattern đang dùng ở TaskGroupPreview.
 */
@Composable
private fun PreviewWrapper(content: @Composable () -> Unit) {
    val mockPopupController = remember { PopupController() }
    Android_Theme {
        CompositionLocalProvider(LocalPopupController provides mockPopupController) {
            content()
        }
    }
}

@Preview(showBackground = true, name = "With tasks — Date tab")
@Composable
private fun TaskManagementScreenPreview() {
    val treeRoots = remember { PreviewDomainEntityData.workspaces.map { it.toTree() } }

    PreviewWrapper {
        TaskManagementScreen(
            treeRoots = treeRoots,
            tasks = PreviewDomainEntityData.tasks,
            selectedDate = Calendar.getInstance(),
            localDate = LocalDate.now(),
            isLoading = false,
            isRefreshing = false,
            error = null,
            onSettingsClick = {},
            onTaskClick = {},
            onAddTaskClick = {},
            onDateClick = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun TaskManagementScreenLoadingPreview() {
    PreviewWrapper {
        TaskManagementScreen(
            treeRoots = emptyList(),
            tasks = emptyList(),
            selectedDate = Calendar.getInstance(),
            localDate = LocalDate.now(),
            isLoading = true,
            isRefreshing = false,
            error = null,
            onSettingsClick = {},
            onTaskClick = {},
            onAddTaskClick = {},
            onDateClick = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty state")
@Composable
private fun TaskManagementScreenEmptyPreview() {
    PreviewWrapper {
        TaskManagementScreen(
            treeRoots = emptyList(),
            tasks = emptyList(),
            selectedDate = Calendar.getInstance(),
            localDate = LocalDate.now(),
            isLoading = false,
            isRefreshing = false,
            error = null,
            onSettingsClick = {},
            onTaskClick = {},
            onAddTaskClick = {},
            onDateClick = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "Error state")
@Composable
private fun TaskManagementScreenErrorPreview() {
    PreviewWrapper {
        TaskManagementScreen(
            treeRoots = emptyList(),
            tasks = emptyList(),
            selectedDate = Calendar.getInstance(),
            localDate = LocalDate.now(),
            isLoading = false,
            isRefreshing = false,
            error = "Network request failed (HTTP 503)",
            onSettingsClick = {},
            onTaskClick = {},
            onAddTaskClick = {},
            onDateClick = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}