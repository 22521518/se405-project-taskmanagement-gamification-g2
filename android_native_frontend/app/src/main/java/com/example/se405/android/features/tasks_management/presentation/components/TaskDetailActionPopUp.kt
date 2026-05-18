@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonCTAText
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.LabelColumnContent
import com.example.se405.android.core.presentation.components.LabelRowContent
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.components.TextFieldApp
import com.example.se405.android.core.presentation.components.formatDate
import com.example.se405.android.core.presentation.components.menu.MenuDropDownApp
import com.example.se405.android.core.presentation.components.menu.MenuSelectionItem
import com.example.se405.android.core.presentation.components.menu.MenuSelectionPopUp
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.BlueGrey80
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
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
)

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
        canChangeTaskType = false,
        onTaskTypeChange = {},
    )
}

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
        canChangeTaskType = canChangeTaskType,
        onTaskTypeChange = onTaskTypeChange,
    )
}

@Composable
fun TaskDetailActionBasePopUp(
    title: String,
    task: Task,
    uiState: TaskDetailActionUiState,
    availableTags: List<Tag>,
    projectsInWorkspace: List<Project>,
    membersInWorkspace: List<WorkspaceMember>,
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
    onOpenDateRangePicker: () -> Unit = {},
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onMaxTagSelectionReached: (Int) -> Unit = {},
) {
    val selectedStartDateMillis = uiState.selectedStartDateMillis
    val selectedDueDateMillis = uiState.selectedDueDateMillis

    val isProject = uiState.taskType == TaskType.PROJECT

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

    PopUpLayout(
        widthRatio = 0.95f,
        heightRatio = 0.9f,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // --- 1. HEADER CỐ ĐỊNH ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = AppText.HeadBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )

            ButtonCTAText(
                onClick = onCancel,
                contentPadding = PaddingValues(4.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_cross_thin),
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))

        // --- 2. BODY CÓ THỂ CUỘN ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextFieldApp(
                labelTitle = "Task Name",
                value = uiState.title,
                onValueChange = onTitleChange,
                maxTextLen = 40,
            )

            LabelRowContent(
                labelName = "Task Type: ",
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                if (canChangeTaskType) {
                    var isTaskTypeExpanded by remember { mutableStateOf(false) }
                    val rotation by animateFloatAsState(
                        targetValue = if (!isTaskTypeExpanded) 180f else 0f,
                        label = "icon rotation"
                    )

                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = uiState.taskType.name, style = AppText.Body2SemiBold)
                            ButtonApp(
                                onClick = { isTaskTypeExpanded = true },
                                type = ButtonType.TEXT,
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_arrow_down),
                                    contentDescription = null,
                                    modifier = Modifier.rotate(rotation).size(16.dp),
                                    tint = BlueGrey80
                                )
                            }
                        }
                        val taskTypeItems = TaskType.entries.map {
                            MenuSelectionItem(data = it, selected = uiState.taskType == it)
                        }
                        MenuDropDownApp(
                            expanded = isTaskTypeExpanded,
                            onExpandedChange = { expanded -> isTaskTypeExpanded = expanded },
                            items = taskTypeItems,
                            onDismiss = { isTaskTypeExpanded = false },
                            onItemClick = { dropdownItem ->
                                onTaskTypeChange(dropdownItem.data)
                                isTaskTypeExpanded = false
                            },
                        ) { item ->
                            Text(item.data.name)
                        }
                    }
                } else {
                    Text(text = uiState.taskType.name, style = AppText.Body2SemiBold)
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = uiState.priority.toString(), style = AppText.Body2SemiBold)
                        ButtonApp(
                            onClick = { isPriorityExpanded = true },
                            type = ButtonType.TEXT,
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_arrow_down),
                                contentDescription = null,
                                modifier = Modifier.rotate(rotation).size(16.dp),
                                tint = BlueGrey80
                            )
                        }
                    }
                    val priorityItems = TaskPriority.entries.map { priority ->
                        MenuSelectionItem(data = priority, selected = uiState.priority == priority)
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
                        Text(item.data.name)
                    }
                }
            }

            if (isProject) {
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedProjectName ?: "Select project",
                                style = AppText.Body2SemiBold,
                                color = colorSelectedItem(selectedProjectName != null)
                            )
                            ButtonApp(
                                onClick = { isProjectExpanded = true },
                                type = ButtonType.TEXT,
                                contentPadding = PaddingValues(0.dp),
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
                            MenuSelectionItem(data = project, selected = project.id == uiState.selectedProjectId)
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedAssigneeName ?: "Select assignee",
                                style = AppText.Body2SemiBold,
                                color = colorSelectedItem(selectedAssigneeName != null)
                            )
                            ButtonApp(
                                onClick = { isAssigneeExpanded = true },
                                type = ButtonType.TEXT,
                                contentPadding = PaddingValues(0.dp),
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
                            MenuSelectionItem(data = member, selected = member.userId == uiState.selectedAssigneeId)
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
                        style = AppText.Body2SemiBold,
                        color = colorSelectedItem(isSelected)
                    )
                }
            } else {
                LabelRowContent(labelName = "Repetition", modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(text = viewTask.repetition.toString(), style = AppText.Body2SemiBold)
                }
            }

            LabelRowContent(labelName = "Tags: ", modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    uiState.selectedTags.forEach { tag ->
                        Row(
                            modifier = Modifier
                                .background(
                                    color = Color(tag.color).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(tag.label.icon),
                                tint = Color(tag.color),
                                contentDescription = tag.name,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = tag.name,
                                color = Color(tag.color),
                                style = AppText.CaptionSemiBold
                            )
                        }
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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        type = ButtonType.OUTLINED,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_plus),
                                contentDescription = "Select",
                                tint = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text("Add", color = MaterialTheme.colorScheme.secondaryContainer, style = AppText.CaptionSemiBold)
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.data.label.name)
                    }
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
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))

        // --- 3. FOOTER CỐ ĐỊNH ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ButtonApp(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                type = ButtonType.OUTLINED,
            ) {
                Text("Cancel", style = AppText.Body2SemiBold)
            }
            ButtonApp(
                onClick = onDone,
                modifier = Modifier.weight(1f),
                type = ButtonType.FILLED,
            ) {
                Text("Save", style = AppText.Body2SemiBold)
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
    LabelColumnContent(labelName = "Start & Due Date: ", iconId = R.drawable.icon_time, content = content, modifier = modifier)
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