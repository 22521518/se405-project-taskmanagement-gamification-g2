@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.se405.android_native_frontend.R
import com.example.se405.android_native_frontend.core.presentation.components.ButtonApp
import com.example.se405.android_native_frontend.core.presentation.components.ButtonCTAText
import com.example.se405.android_native_frontend.core.presentation.components.ButtonType
import com.example.se405.android_native_frontend.core.presentation.components.LabelColumnContent
import com.example.se405.android_native_frontend.core.presentation.components.LabelRowContent
import com.example.se405.android_native_frontend.core.presentation.components.TextFieldApp
import com.example.se405.android_native_frontend.core.presentation.components.formatDate
import com.example.se405.android_native_frontend.core.presentation.components.menu.MenuDropDownApp
import com.example.se405.android_native_frontend.core.presentation.components.menu.MenuSelectionItem
import com.example.se405.android_native_frontend.core.presentation.components.menu.MenuSelectionPopUp
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme
import com.example.se405.android_native_frontend.core.presentation.theme.AppText
import com.example.se405.android_native_frontend.core.presentation.theme.BlueGrey80
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.PreviewDomainEntityData
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Project
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.WorkspaceMember
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val DEFAULT_MAX_TAGS = 3

data class TaskDetailActionUiState(
    val title: String,
    val selectedTags: List<Tag>,
    val priority: TaskPriority,
    val description: String,
    val selectedStartDateMillis: Long?,
    val selectedDueDateMillis: Long?,
    val isTagSelectorVisible: Boolean,
    val selectedProjectId: Uuid?,
    val selectedAssigneeId: Uuid?,
)

/**
 * Edit popup wrapper for task details.
 *
 * This composable is intentionally stateless regarding business logic:
 * all user interactions are delegated through callbacks so Screen/ViewModel
 * can coordinate side effects and persistence.
 *
 * API mapping note when both `task` and `uiState` are provided:
 * - Keep immutable identity/scope fields from `task` (e.g. task id, type).
 * - Use editable fields from `uiState` for request payload
 *   (`title`, `description`, `priority`, selected `tagIds`).
 * - Build request DTO in Screen/ViewModel, not in this composable.
 */
@Composable
fun TaskDetailEditPopUp(
    task: Task,
    uiState: TaskDetailActionUiState,
    availableTags: List<Tag>,
    projectsInWorkspace: List<Project>,
    membersInWorkspace: List<WorkspaceMember>,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onToggleTag: (Tag) -> Unit,
    onProjectSelected: (Uuid) -> Unit,
    onAssigneeSelected: (Uuid) -> Unit,
    onTagSelectorVisibilityChange: (Boolean) -> Unit,
    onOpenDateRangePicker: () -> Unit = {},
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onMaxTagSelectionReached: (Int) -> Unit = {},
) {
    TaskDetailActionBasePopUp(
        title = "Edit task",
        task = task,
        uiState = uiState,
        availableTags = availableTags,
        projectsInWorkspace = projectsInWorkspace,
        membersInWorkspace = membersInWorkspace,
        onTitleChange = onTitleChange,
        onDescriptionChange = onDescriptionChange,
        onPriorityChange = onPriorityChange,
        onToggleTag = onToggleTag,
        onProjectSelected = onProjectSelected,
        onAssigneeSelected = onAssigneeSelected,
        onTagSelectorVisibilityChange = onTagSelectorVisibilityChange,
        onOpenDateRangePicker = onOpenDateRangePicker,
        onDone = onDone,
        onCancel = onCancel,
        onMaxTagSelectionReached = onMaxTagSelectionReached,
    )
}

/**
 * Create popup wrapper for task draft details.
 *
 * Use this for create flow while keeping the same event contract as edit flow.
 */
@Composable
fun TaskDetailCreatePopUp(
    taskDraft: Task,
    uiState: TaskDetailActionUiState,
    availableTags: List<Tag>,
    projectsInWorkspace: List<Project>,
    membersInWorkspace: List<WorkspaceMember>,
    isProject: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onToggleTag: (Tag) -> Unit,
    onProjectSelected: (Uuid) -> Unit,
    onAssigneeSelected: (Uuid) -> Unit,
    onTagSelectorVisibilityChange: (Boolean) -> Unit,
    onOpenDateRangePicker: () -> Unit = {},
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onMaxTagSelectionReached: (Int) -> Unit = {},
) {
    TaskDetailActionBasePopUp(
        title = "Create new task",
        task = taskDraft,
        uiState = uiState,
        availableTags = availableTags,
        projectsInWorkspace = projectsInWorkspace,
        membersInWorkspace = membersInWorkspace,
        isProject = isProject,
        onTitleChange = onTitleChange,
        onDescriptionChange = onDescriptionChange,
        onPriorityChange = onPriorityChange,
        onToggleTag = onToggleTag,
        onProjectSelected = onProjectSelected,
        onAssigneeSelected = onAssigneeSelected,
        onTagSelectorVisibilityChange = onTagSelectorVisibilityChange,
        onOpenDateRangePicker = onOpenDateRangePicker,
        onDone = onDone,
        onCancel = onCancel,
        onMaxTagSelectionReached = onMaxTagSelectionReached,
    )
}

/**
 * Base UI for both create and edit task detail popups.
 *
 * Architecture note:
 * - Receives immutable UI state from upper layer.
 * - Emits user intents only via callbacks.
 * - Does not access repository, ViewModel, or global navigation/popup state directly.
 *
 * API note:
 * - `task` is treated as source of immutable/base fields.
 * - `uiState` is treated as source of user-edited fields.
 * - On save, Screen/ViewModel should map these into API payload.
 * - `projectsInWorkspace` / `membersInWorkspace` should come from
 *   `getProjectsByWorkspace` / `getMembersByWorkspace` in Screen/ViewModel.
 * - Date picker popup should be opened from Screen via PopupController.
 *   This composable only emits `onOpenDateRangePicker` intent.
 */
@Composable
fun TaskDetailActionBasePopUp(
    title: String,
    task: Task,
    uiState: TaskDetailActionUiState,
    availableTags: List<Tag>,
    projectsInWorkspace: List<Project>,
    membersInWorkspace: List<WorkspaceMember>,
    isProject: Boolean = false,
    maxTagSelection: Int = DEFAULT_MAX_TAGS,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onToggleTag: (Tag) -> Unit,
    onProjectSelected: (Uuid) -> Unit,
    onAssigneeSelected: (Uuid) -> Unit,
    onTagSelectorVisibilityChange: (Boolean) -> Unit,
    onOpenDateRangePicker: () -> Unit = {},
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onMaxTagSelectionReached: (Int) -> Unit = {},
) {
    val selectedStartDateMillis = uiState.selectedStartDateMillis
    val selectedDueDateMillis = uiState.selectedDueDateMillis

    val viewTask = task.copy(
        title = uiState.title,
        priority = uiState.priority,
        tags = uiState.selectedTags,
        description = uiState.description,
        startDate = selectedStartDateMillis?.let(::toLocalDate) ?: task.startDate,
        dueDate = selectedDueDateMillis?.let(::toLocalDate) ?: task.dueDate,
    )
    val selectedProjectName = projectsInWorkspace
        .firstOrNull { it.id == uiState.selectedProjectId }
        ?.name
    
    val selectedAssigneeName = membersInWorkspace
        .firstOrNull { it.userId == uiState.selectedAssigneeId }
        ?.user
        ?.displayName

    BoxWithConstraints {
        val targetWidth = maxWidth * 0.80f
        val targetMaxHeight = maxHeight * 0.90f
        Column(
            modifier = Modifier
                .widthIn(min = 300.dp, max = targetWidth)
                .heightIn(max = targetMaxHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.clickable(onClick = onCancel),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        style = AppText.HeadBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }

                ButtonCTAText(
                    onClick = onCancel,
                    contentPadding = PaddingValues(2.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_cross_thin),
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            TextFieldApp(
                labelTitle = "Task Name",
                value = uiState.title,
                onValueChange = onTitleChange,
                maxTextLen = 20,
            )

            LabelRowContent(labelName = "Tags: ", modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    uiState.selectedTags.forEach { tag ->
                        Icon(
                            painter = painterResource(tag.label.icon),
                            tint = Color(tag.color),
                            contentDescription = tag.name,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    ButtonApp(
                        onClick = {
                            if (uiState.selectedTags.size >= maxTagSelection) {
                                onMaxTagSelectionReached(maxTagSelection)
                            } else {
                                onTagSelectorVisibilityChange(true)
                            }
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        type = ButtonType.OUTLINED,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_plus),
                                contentDescription = "Select",
                                tint = MaterialTheme.colorScheme.secondaryContainer,
                            )
                            Text("Select", color = MaterialTheme.colorScheme.secondaryContainer)
                        }
                    }
                }
            }

            if (uiState.isTagSelectorVisible) {
                val menuItems = availableTags.map { tag ->
                    MenuSelectionItem(
                        data = tag,
                        selected = uiState.selectedTags.any { selectedTag -> selectedTag.uuid == tag.uuid },
                    )
                }

                MenuSelectionPopUp(
                    items = menuItems,
                    onDismiss = { onTagSelectorVisibilityChange(false) },
                    onItemClick = { menuItem -> onToggleTag(menuItem.data) },
                ) { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(item.data.label.icon),
                            contentDescription = item.data.name,
                            tint = Color(item.data.color),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.data.label.name)
                    }
                }
            }

            LabelRowContent(
                labelName = "Priority: ",
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                var isPriorityExpanded by remember { mutableStateOf(false) }
                val rotation by animateFloatAsState(
                    targetValue = if (!isPriorityExpanded) 180f else 0f,
                    label = "icon rotation"
                )

                Box {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = uiState.priority.toString(), style = AppText.CaptionRegular)
                        ButtonApp(
                            onClick = { isPriorityExpanded = true },
                            type = ButtonType.TEXT,
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(
                                0.dp,
                                MaterialTheme.colorScheme.secondaryContainer
                            ),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_arrow_down),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .rotate(rotation)
                                        .size(16.dp),
                                    tint = BlueGrey80
                                )
                            }
                        }
                    }
                    val priorityItems = TaskPriority.entries.map { priority ->
                        MenuSelectionItem(
                            data = priority,
                            selected = uiState.priority == priority,
                        )
                    }
                    MenuDropDownApp(
                        expanded = isPriorityExpanded,
                        onExpandedChange = { expanded -> isPriorityExpanded = expanded },
                        items = priorityItems,
                        onDismiss = { isPriorityExpanded = false },
                        onItemClick = { dropdownItem ->
                            onPriorityChange(dropdownItem.data)
                            isPriorityExpanded = false
                        },
                    ) { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(item.data.name)
                        }
                    }
                }
            }

            if (!isProject) {
                LabelRowContent(
                    labelName = "Project: ",
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    var isProjectExpanded by remember { mutableStateOf(false) }
                    val rotation by animateFloatAsState(
                        targetValue = if (!isProjectExpanded) 180f else 0f,
                        label = "icon rotation"
                    )

                    Box {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = selectedProjectName ?: "Select project",
                                style = AppText.CaptionRegular,
                                color = colorSelectedItem(selectedProjectName != null)
                            )
                            ButtonApp(
                                onClick = { isProjectExpanded = true },
                                type = ButtonType.TEXT,
                                contentPadding = PaddingValues(0.dp),
                                border = BorderStroke(
                                    0.dp,
                                    MaterialTheme.colorScheme.secondaryContainer
                                ),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_arrow_down),
                                    contentDescription = "Select project",
                                    tint = BlueGrey80,
                                    modifier = Modifier.size(16.dp).rotate(rotation),
                                )
                            }
                        }

                        val projectItems = projectsInWorkspace.map { project ->
                            MenuSelectionItem(
                                data = project,
                                selected = project.id == uiState.selectedProjectId,
                            )
                        }
                        MenuDropDownApp(
                            expanded = isProjectExpanded,
                            onExpandedChange = { expanded -> isProjectExpanded = expanded },
                            items = projectItems,
                            onDismiss = { isProjectExpanded = false },
                            onItemClick = { dropdownItem ->
                                onProjectSelected(dropdownItem.data.id)
                                isProjectExpanded = false
                            },
                        ) { item ->
                            Text(
                                item.data.name,
                                style = if (item.selected) AppText.CaptionBold else AppText.CaptionRegular
                            )
                        }
                    }
                }

                LabelRowContent(
                    labelName = "Assignee: ",
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    var isAssigneeExpanded by remember { mutableStateOf(false) }
                    val rotation by animateFloatAsState(
                        targetValue = if (!isAssigneeExpanded) 180f else 0f,
                        label = "icon rotation"
                    )

                    Box {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = selectedAssigneeName ?: "Select assignee",
                                style = AppText.CaptionRegular,
                                color = colorSelectedItem(selectedAssigneeName != null)
                            )
                            ButtonApp(
                                onClick = { isAssigneeExpanded = true },
                                type = ButtonType.TEXT,
                                contentPadding = PaddingValues(0.dp),
                                border = BorderStroke(
                                    0.dp,
                                    MaterialTheme.colorScheme.secondaryContainer
                                ),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_arrow_down),
                                    contentDescription = "Select assignee",
                                    tint = BlueGrey80,
                                    modifier = Modifier.rotate(rotation).size(16.dp),
                                )
                            }
                        }

                        val memberItems = membersInWorkspace.map { member ->
                            MenuSelectionItem(
                                data = member,
                                selected = member.userId == uiState.selectedAssigneeId,
                            )
                        }
                        MenuDropDownApp(
                            expanded = isAssigneeExpanded,
                            onExpandedChange = { expanded -> isAssigneeExpanded = expanded },
                            items = memberItems,
                            onDismiss = { isAssigneeExpanded = false },
                            onItemClick = { dropdownItem ->
                                onAssigneeSelected(dropdownItem.data.userId)
                                isAssigneeExpanded = false
                            },
                        ) { item ->
                            Text(
                                item.data.user?.displayName ?: item.data.userId.toString(),
                                style = if (item.selected) AppText.CaptionBold else AppText.CaptionRegular
                            )
                        }
                    }
                }
                DueDatePickerSection(
                        modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .clickable { onOpenDateRangePicker() },
                ) {
                    val isSelected = selectedStartDateMillis != null && selectedDueDateMillis != null
                    Text(
                        text = if (isSelected) {
                            "${formatDate(selectedStartDateMillis)} - ${formatDate(selectedDueDateMillis)}"
                        } else {
                            "No date range selected"
                        },
                        color = colorSelectedItem(isSelected)
                    )
                }
            } else {
                LabelRowContent(labelName = "Repetition") {
                    Text(text = viewTask.repetition.toString())
                }
            }

            TextFieldApp(
                labelTitle = "Description",
                onValueChange = onDescriptionChange,
                singleLine = false,
                maxLines = 5,
                value = viewTask.description,
                style = AppText.CaptionRegular.copy(lineHeight = 20.sp, color = MaterialTheme.colorScheme.surface),
                maxTextLen = 500
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ButtonApp(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    type = ButtonType.OUTLINED,
                ) {
                    Text("Cancel")
                }
                ButtonApp(
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                    type = ButtonType.FILLED,
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun colorSelectedItem(isSelected: Boolean): Color {
    if (isSelected)
        return MaterialTheme.colorScheme.secondaryContainer
    return MaterialTheme.colorScheme.surface
}

@Composable
private fun DueDatePickerSection(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    LabelColumnContent(labelName = "Start: ", iconId = R.drawable.icon_time, content = content, modifier = modifier)
}


/**
 * Preview showing local state hoisting pattern for this popup.
 *
 * Usage guide:
 * - In production, uiState should come from ViewModel state flow.
 * - onTitleChange/onToggleTag/onPriorityChange should dispatch to ViewModel intents.
 * - onDone/onCancel should be handled by Screen-level coordinator (navigation/popup host).
 * - open date picker should be handled by Screen via `LocalPopupController.current.push { ... }`.
 * - When save is clicked, Screen/ViewModel should map payload like:
 *   taskId <- task.uuid
 *   title <- uiState.title
 *   description <- uiState.description
 *   priority <- uiState.priority
 *   projectId <- uiState.selectedProjectId (for project tasks)
 *   assigneeId <- uiState.selectedAssigneeId (for project tasks)
 *   startDate <- uiState.selectedStartDateMillis?.let(::toLocalDate)
 *   dueDate <- uiState.selectedDueDateMillis?.let(::toLocalDate)
 *   tagIds <- uiState.selectedTags.map { it.uuid }
 *
 * PopupController usage for DateRangePickerPopUp (in Screen):
 * - `val popup = LocalPopupController.current`
 * - On `onOpenDateRangePicker`, call:
 *   `popup.push { onDismiss -> DateRangePickerPopUp(..., onDismiss = onDismiss) }`
 * - In `onDateRangeSelected`, update ViewModel state for
 *   `selectedStartDateMillis` and `selectedDueDateMillis`.
 */
@Preview(showBackground = true, backgroundColor = 0xFFECF7FB)
@Composable
fun TaskDetailEditPopUpPreview() {
    val previewTask = PreviewDomainEntityData.tasks[0]
    var uiState by remember {
        mutableStateOf(
            TaskDetailActionUiState(
                title = previewTask.title,
                selectedTags = previewTask.tags,
                priority = previewTask.priority,
                description = previewTask.description,
                selectedStartDateMillis = previewTask.startDate.toEpochMillisAtStartOfDay(),
                selectedDueDateMillis = previewTask.dueDate.toEpochMillisAtStartOfDay(),
                isTagSelectorVisible = false,
                selectedProjectId = null,
                selectedAssigneeId = null,
            ),
        )
    }

    Android_native_frontendTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TaskDetailActionBasePopUp(
                title = "Create new task",
                task = previewTask,
                uiState = uiState,
                availableTags = PreviewDomainEntityData.tags,
                projectsInWorkspace = PreviewDomainEntityData.projects,
                membersInWorkspace = PreviewDomainEntityData.workspaceMembers,
                onTitleChange = { newTitle -> uiState = uiState.copy(title = newTitle) },
                onDescriptionChange = { newDescription -> uiState = uiState.copy(description = newDescription) },
                onPriorityChange = { newPriority -> uiState = uiState.copy(priority = newPriority) },
                onToggleTag = { tag ->
                    val isSelected = uiState.selectedTags.any { selectedTag -> selectedTag.uuid == tag.uuid }
                    uiState = uiState.copy(
                        selectedTags = if (isSelected) {
                            uiState.selectedTags.filter { selectedTag -> selectedTag.uuid != tag.uuid }
                        } else {
                            uiState.selectedTags + tag
                        },
                    )
                },
                onProjectSelected = { projectId -> uiState = uiState.copy(selectedProjectId = projectId) },
                onAssigneeSelected = { assigneeId -> uiState = uiState.copy(selectedAssigneeId = assigneeId) },
                onTagSelectorVisibilityChange = { visible -> uiState = uiState.copy(isTagSelectorVisible = visible) },
                onOpenDateRangePicker = {},
                onDone = {},
                onCancel = {},
            )
        }
    }
}

private fun toLocalDate(timestamp: Long): LocalDate {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

private fun LocalDate.toEpochMillisAtStartOfDay(): Long {
    return atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
