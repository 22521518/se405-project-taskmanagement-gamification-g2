@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.components.formatDate
import com.example.se405.android.core.presentation.components.menu.MenuDropDownApp
import com.example.se405.android.core.presentation.components.menu.MenuSelectionItem
import com.example.se405.android.core.presentation.components.menu.MenuSelectionPopUp
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.Blue40
import com.example.se405.android.core.presentation.theme.Blue60
import com.example.se405.android.core.presentation.theme.Blue80
import com.example.se405.android.core.presentation.theme.Blue90
import com.example.se405.android.core.presentation.theme.BlueGrey70
import com.example.se405.android.core.presentation.theme.BlueGrey80
import com.example.se405.android.core.presentation.theme.White
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.workspaces_management.presentation.components.DateChip
import com.example.se405.android.features.workspaces_management.presentation.components.TagChip
import com.example.se405.android.features.workspaces_management.presentation.components.TagChipUi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val DEFAULT_MAX_TAGS = 3

data class TaskDetailActionUiState(
    val title: String,
    val taskType: TaskType,
    val selectedTags: List<Tag>,
    val priority: TaskPriority,
    val description: String,
    val selectedStartDateMillis: Long?,
    val selectedDueDateMillis: Long?,
    val isTagSelectorVisible: Boolean,
    val selectedProjectId: Uuid?,
    val selectedAssigneeId: Uuid?,
    val repetition: Int,
)

/**
 * Edit popup wrapper for task details.
 *
 * Stateless wrt business logic: every interaction is delegated to callbacks so
 * the Screen/ViewModel can coordinate side effects and persistence.
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
    onCreateTagClick: () -> Unit = {},
    onOpenDateRangePicker: () -> Unit = {},
    onRepetitionChange: (Int) -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onMaxTagSelectionReached: (Int) -> Unit = {},
    onEditTagClick: (Tag) -> Unit = {},
    onDeleteTagClick: (Tag) -> Unit = {},
    isLoading: Boolean = false,
) {
    TaskDetailActionBasePopUp(
        title = "Edit task",
        subtitle = "Update the task details below.",
        confirmLabel = "Save",
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
        onCreateTagClick = onCreateTagClick,
        onOpenDateRangePicker = onOpenDateRangePicker,
        onRepetitionChange = onRepetitionChange,
        onDone = onDone,
        onCancel = onCancel,
        onMaxTagSelectionReached = onMaxTagSelectionReached,
        canChangeTaskType = false,
        onTaskTypeChange = {},
        onEditTagClick = onEditTagClick,
        onDeleteTagClick = onDeleteTagClick,
        isLoading = isLoading,
    )
}

/**
 * Create popup wrapper for a task draft.
 *
 * Same event contract as the edit flow so callers stay symmetric.
 */
@Composable
fun TaskDetailCreatePopUp(
    taskDraft: Task,
    uiState: TaskDetailActionUiState,
    availableTags: List<Tag>,
    projectsInWorkspace: List<Project>,
    membersInWorkspace: List<WorkspaceMember>,
    canChangeTaskType: Boolean = true,
    onTaskTypeChange: (TaskType) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onToggleTag: (Tag) -> Unit,
    onProjectSelected: (Uuid) -> Unit,
    onAssigneeSelected: (Uuid) -> Unit,
    onTagSelectorVisibilityChange: (Boolean) -> Unit,
    onCreateTagClick: () -> Unit = {},
    onOpenDateRangePicker: () -> Unit = {},
    onRepetitionChange: (Int) -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onMaxTagSelectionReached: (Int) -> Unit = {},
    onEditTagClick: (Tag) -> Unit = {},
    onDeleteTagClick: (Tag) -> Unit = {},
    isLoading: Boolean = false,
) {
    TaskDetailActionBasePopUp(
        title = "Create new task",
        subtitle = "Add a task to track progress.",
        confirmLabel = "Create",
        task = taskDraft,
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
        onCreateTagClick = onCreateTagClick,
        onOpenDateRangePicker = onOpenDateRangePicker,
        onRepetitionChange = onRepetitionChange,
        onDone = onDone,
        onCancel = onCancel,
        onMaxTagSelectionReached = onMaxTagSelectionReached,
        canChangeTaskType = canChangeTaskType,
        onTaskTypeChange = onTaskTypeChange,
        onEditTagClick = onEditTagClick,
        onDeleteTagClick = onDeleteTagClick,
        isLoading = isLoading,
    )
}

/**
 * Base UI for both create and edit task detail popups.
 *
 * Restyled to share the design language of `CreateTaskPopUpContent`:
 *  - DisplayBold header with a supporting subtitle.
 *  - BodyBold field labels above `OutlinedTextField`s.
 *  - Segmented chip selectors for task type and priority.
 *  - Boxed dropdown fields for project / assignee.
 *  - Reused `DateChip` range selector and `TagChip`s.
 *  - Weighted Cancel / confirm action buttons with an inline loading spinner.
 *
 * Architecture note: receives immutable UI state and only emits intents via
 * callbacks. It does not touch repositories, ViewModels, or global popup state.
 */
@Composable
fun TaskDetailActionBasePopUp(
    title: String,
    task: Task,
    uiState: TaskDetailActionUiState,
    availableTags: List<Tag>,
    projectsInWorkspace: List<Project>,
    membersInWorkspace: List<WorkspaceMember>,
    subtitle: String = "Add a task to track progress.",
    confirmLabel: String = "Save",
    canChangeTaskType: Boolean = false,
    maxTagSelection: Int = DEFAULT_MAX_TAGS,
    onTaskTypeChange: (TaskType) -> Unit = {},
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onToggleTag: (Tag) -> Unit,
    onProjectSelected: (Uuid) -> Unit,
    onAssigneeSelected: (Uuid) -> Unit,
    onTagSelectorVisibilityChange: (Boolean) -> Unit,
    onCreateTagClick: () -> Unit = {},
    onOpenDateRangePicker: () -> Unit = {},
    onRepetitionChange: (Int) -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onMaxTagSelectionReached: (Int) -> Unit = {},
    onEditTagClick: (Tag) -> Unit = {},
    onDeleteTagClick: (Tag) -> Unit = {},
    isLoading: Boolean = false,
) {
    val isProject = uiState.taskType == TaskType.PROJECT

    val selectedProjectName = projectsInWorkspace
        .firstOrNull { it.id == uiState.selectedProjectId }
        ?.name

    val selectedAssigneeName = membersInWorkspace
        .firstOrNull { it.userId == uiState.selectedAssigneeId }
        ?.user
        ?.displayName

    if (uiState.isTagSelectorVisible) {
        TagSelectorPopUp(
            availableTags = availableTags,
            selectedTags = uiState.selectedTags,
            onToggleTag = onToggleTag,
            onEditTagClick = onEditTagClick,
            onDeleteTagClick = onDeleteTagClick,
            onDismiss = { onTagSelectorVisibilityChange(false) },
        )
    }

    PopUpLayout {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = AppText.DisplayBold)
                Text(text = subtitle, style = AppText.Body2Regular)
            }

            // Title
            Column {
                Text(text = "Task title*", style = AppText.BodyBold)
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { if (it.length <= 50) onTitleChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    placeholder = { Text("Enter task title") },
                    singleLine = true,
                )
            }

            // Description
            Column {
                Text(text = "Description", style = AppText.BodyBold)
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { if (it.length <= 500) onDescriptionChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 80.dp),
                    placeholder = { Text("Optional description…") },
                    maxLines = 4,
                )
            }

            // Task type
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Task type", style = AppText.BodyBold)
                TaskTypeSelector(
                    selected = uiState.taskType,
                    enabled = canChangeTaskType,
                    onSelect = onTaskTypeChange,
                )
            }

            // Priority
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Priority", style = AppText.BodyBold)
                DomainPrioritySelector(selected = uiState.priority, onSelect = onPriorityChange)
            }

            if (isProject) {
                // Project
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Project*", style = AppText.BodyBold)
                    DropdownField(
                        valueText = selectedProjectName,
                        placeholder = "Select project",
                        items = projectsInWorkspace.map {
                            MenuSelectionItem(data = it, selected = it.id == uiState.selectedProjectId)
                        },
                        onItemSelected = { onProjectSelected(it.id) },
                    ) { item ->
                        Text(
                            item.data.name,
                            style = if (item.selected) AppText.BodyBold else AppText.BodyRegular,
                        )
                    }
                }

                // Assignee
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Assignee", style = AppText.BodyBold)
                    DropdownField(
                        valueText = selectedAssigneeName,
                        placeholder = "Select assignee",
                        items = membersInWorkspace.map {
                            MenuSelectionItem(data = it, selected = it.userId == uiState.selectedAssigneeId)
                        },
                        onItemSelected = { onAssigneeSelected(it.userId) },
                    ) { item ->
                        Text(
                            item.data.user?.displayName ?: item.data.userId.toString(),
                            style = if (item.selected) AppText.BodyBold else AppText.BodyRegular,
                        )
                    }
                }

                // Date range
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Date range", style = AppText.BodyBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DateChip(
                            label = "Start",
                            date = uiState.selectedStartDateMillis?.let(::formatDate),
                            modifier = Modifier.weight(1f),
                            onClick = onOpenDateRangePicker,
                        )
                        DateChip(
                            label = "Due",
                            date = uiState.selectedDueDateMillis?.let(::formatDate),
                            modifier = Modifier.weight(1f),
                            onClick = onOpenDateRangePicker,
                        )
                    }
                }
            } else {
                // Repetition (HABIT)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Repetition", style = AppText.BodyBold)
                    OutlinedTextField(
                        value = if (uiState.repetition > 0) uiState.repetition.toString() else "",
                        onValueChange = { text ->
                            val repetition = text.filter { it.isDigit() }.take(3).toIntOrNull() ?: 0
                            onRepetitionChange(repetition)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Times per day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            }

            // Tags
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "Tags", style = AppText.BodyBold)
                    Row {
                        TextButton(
                            onClick = {
                                if (uiState.selectedTags.size >= maxTagSelection) {
                                    onMaxTagSelectionReached(maxTagSelection)
                                } else {
                                    onTagSelectorVisibilityChange(true)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_plus),
                                contentDescription = "Select tag",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Select", style = AppText.Body2Regular, color = MaterialTheme.colorScheme.onPrimary)
                        }
                        TextButton(
                            onClick = onCreateTagClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("New tag", style = AppText.Body2Regular, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }

                if (uiState.selectedTags.isEmpty()) {
                    Text(
                        text = "No tags selected.",
                        style = AppText.Body2Regular,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.selectedTags) { tag ->
                            TagChip(
                                tag = TagChipUi(color = tag.color, name = tag.name),
                                isSelected = true,
                                enabled = true,
                                onClick = { onToggleTag(tag) }
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ButtonApp(
                    onClick = { if (!isLoading) onCancel() },
                    modifier = Modifier.weight(1f),
                    type = ButtonType.OUTLINED
                ) { Text("Cancel") }
                ButtonApp(
                    type = ButtonType.FILLED,
                    modifier = Modifier.weight(1f),
                    onClick = { if (!isLoading) onDone() }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(confirmLabel)
                    }
                }
            }
        }
    }
}

// ─── Task type selector ─────────────────────────────────────────────────────

@Composable
private fun TaskTypeSelector(
    selected: TaskType,
    enabled: Boolean,
    onSelect: (TaskType) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskType.entries.forEach { type ->
            val isSelected = type == selected
            val label = type.name.lowercase().replaceFirstChar { it.uppercase() }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled && !isSelected) { onSelect(type) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Blue40 else Blue90,
                border = if (isSelected) BorderStroke(1.5.dp, Blue40) else BorderStroke(1.dp, BlueGrey80),
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    style = AppText.BodySemiBold,
                    color = if (isSelected) White else BlueGrey70,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ─── Priority selector (domain TaskPriority) ────────────────────────────────

@Composable
private fun DomainPrioritySelector(
    selected: TaskPriority,
    onSelect: (TaskPriority) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskPriority.entries.forEach { priority ->
            val isSelected = priority == selected
            val (bgColor, contentColor, label) = when (priority) {
                TaskPriority.LOW -> Triple(Blue90, Blue60, "Low")
                TaskPriority.MEDIUM -> Triple(Blue80, Blue40, "Medium")
                TaskPriority.HIGH -> Triple(Blue40, White, "High")
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(priority) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) bgColor else Blue90,
                border = if (isSelected) BorderStroke(1.5.dp, bgColor.copy(alpha = 0.7f)) else BorderStroke(1.dp, BlueGrey80),
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    style = AppText.BodySemiBold,
                    color = if (isSelected) contentColor else BlueGrey70,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ─── Boxed dropdown field (project / assignee) ──────────────────────────────

@Composable
private fun <T> DropdownField(
    valueText: String?,
    placeholder: String,
    items: List<MenuSelectionItem<T>>,
    onItemSelected: (T) -> Unit,
    itemLabel: @Composable (MenuSelectionItem<T>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Blue90)
                .border(1.dp, BlueGrey80, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = valueText ?: placeholder,
                style = AppText.BodySemiBold,
                color = if (valueText != null) Color.Black else Color.DarkGray,
                modifier = Modifier.weight(1f),
            )
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 0f else 180f,
                label = "dropdown rotation"
            )
            Icon(
                painter = painterResource(id = R.drawable.icon_arrow_down),
                contentDescription = null,
                modifier = Modifier.rotate(rotation).size(16.dp),
                tint = BlueGrey80,
            )
        }
        MenuDropDownApp(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            items = items,
            onDismiss = { expanded = false },
            onItemClick = { item ->
                onItemSelected(item.data)
                expanded = false
            },
            itemLabel = itemLabel,
        )
    }
}

// ─── Tag selector popup (select / edit / delete) ────────────────────────────

@Composable
private fun TagSelectorPopUp(
    availableTags: List<Tag>,
    selectedTags: List<Tag>,
    onToggleTag: (Tag) -> Unit,
    onEditTagClick: (Tag) -> Unit,
    onDeleteTagClick: (Tag) -> Unit,
    onDismiss: () -> Unit,
) {
    val menuItems = availableTags.map { tag ->
        MenuSelectionItem(
            data = tag,
            selected = selectedTags.any { it.uuid == tag.uuid },
        )
    }
    MenuSelectionPopUp(
        items = menuItems,
        onDismiss = onDismiss,
        onItemClick = { onToggleTag(it.data) },
    ) { item ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(item.data.color))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.data.name, color = MaterialTheme.colorScheme.onSurface, style = AppText.BodyRegular)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_edit_pencil),
                    contentDescription = "Edit Tag",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp).clickable { onEditTagClick(item.data) }
                )
                Icon(
                    painter = painterResource(id = R.drawable.icon_garbage),
                    contentDescription = "Delete Tag",
                    tint = Color.Red.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp).clickable { onDeleteTagClick(item.data) }
                )
                AnimatedVisibility(visible = item.selected) {
                    Icon(
                        painter = painterResource(R.drawable.icon_done),
                        tint = Blue40,
                        contentDescription = "Selected",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Mapping helpers ────────────────────────────────────────────────────────

private fun toLocalDate(timestamp: Long): LocalDate {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun LocalDate.toEpochMillisAtStartOfDay(): Long {
    return atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun TaskDetailActionUiState.toDomainTask(
    baseTask: Task,
    status: TaskStatus = baseTask.status,
    uuid: Uuid = baseTask.uuid,
    membersInWorkspace: List<WorkspaceMember> = emptyList(),
): Task {
    val startDate = selectedStartDateMillis?.let(::toLocalDate) ?: baseTask.startDate
    val dueDate = selectedDueDateMillis?.let(::toLocalDate) ?: baseTask.dueDate
    val resolvedProjectId =
        if (taskType == TaskType.PROJECT) selectedProjectId ?: baseTask.projectId else null

    val assigneeUser = selectedAssigneeId?.let { assigneeId ->
        membersInWorkspace.firstOrNull { it.userId == assigneeId }?.user
    }
    val newAssignees = listOfNotNull(assigneeUser)

    return baseTask.copy(
        uuid = uuid,
        title = title,
        description = description,
        type = taskType,
        status = status,
        priority = priority,
        tags = selectedTags,
        assignees = newAssignees,
        startDate = startDate,
        dueDate = dueDate,
        projectId = resolvedProjectId,
        repetition = repetition,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFECF7FB)
@Composable
fun TaskDetailEditPopUpPreview() {
    val previewTask = PreviewDomainEntityData.tasks[0]

    var uiState by remember {
        mutableStateOf(
            TaskDetailActionUiState(
                title = previewTask.title,
                taskType = previewTask.type,
                selectedTags = previewTask.tags,
                priority = previewTask.priority,
                description = previewTask.description,
                selectedStartDateMillis = previewTask.startDate?.toEpochMillisAtStartOfDay(),
                selectedDueDateMillis = previewTask.dueDate?.toEpochMillisAtStartOfDay(),
                isTagSelectorVisible = false,
                selectedProjectId = null,
                selectedAssigneeId = null,
                repetition = previewTask.repetition
            ),
        )
    }

    Android_Theme {
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
                canChangeTaskType = true,
                onTaskTypeChange = { uiState = uiState.copy(taskType = it) },
                onTitleChange = { uiState = uiState.copy(title = it) },
                onDescriptionChange = { uiState = uiState.copy(description = it) },
                onPriorityChange = { uiState = uiState.copy(priority = it) },
                onToggleTag = { tag ->
                    val isSelected = uiState.selectedTags.any { it.uuid == tag.uuid }
                    uiState = uiState.copy(
                        selectedTags = if (isSelected) {
                            uiState.selectedTags.filter { it.uuid != tag.uuid }
                        } else {
                            uiState.selectedTags + tag
                        },
                    )
                },
                onProjectSelected = { uiState = uiState.copy(selectedProjectId = it) },
                onAssigneeSelected = { uiState = uiState.copy(selectedAssigneeId = it) },
                onTagSelectorVisibilityChange = { uiState = uiState.copy(isTagSelectorVisible = it) },
                onCreateTagClick = {},
                onOpenDateRangePicker = {},
                onDone = {},
                onCancel = {},
                onRepetitionChange = { uiState = uiState.copy(repetition = it) }
            )
        }
    }
}
