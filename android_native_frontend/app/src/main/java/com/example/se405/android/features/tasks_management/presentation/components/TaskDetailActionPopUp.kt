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
<<<<<<< HEAD
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
=======
import androidx.compose.foundation.text.KeyboardOptions
>>>>>>> origin/dev
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
<<<<<<< HEAD
=======
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
>>>>>>> origin/dev
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
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
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
    val repetition: Int,
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
        repetition = uiState.repetition,
    )
    val selectedProjectName = projectsInWorkspace
        .firstOrNull { it.id == uiState.selectedProjectId }
        ?.name

    val selectedAssigneeName = membersInWorkspace
        .firstOrNull { it.userId == uiState.selectedAssigneeId }
        ?.user
        ?.displayName

    PopUpLayout(
<<<<<<< HEAD
        widthRatio = 0.95f,
        heightRatio = 0.9f,
        verticalArrangement = Arrangement.spacedBy(0.dp),
=======
        verticalArrangement = Arrangement.spacedBy(6.dp),
>>>>>>> origin/dev
    ) {
        // --- 1. HEADER CỐ ĐỊNH ---
        Row(
<<<<<<< HEAD
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
=======
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Cancel", style = AppText.BodySemiBold, color = Color.Gray, modifier = Modifier.clickable(onClick = onCancel))
            Text(
                text = title,
                style = AppText.HeadBold,
                color = Color.Black ,
            )
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            }
            else {
                Text(text = "Done", style = AppText.BodySemiBold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.clickable{ onDone() })
            }
        }

        TextFieldApp(
            labelTitle = "Task Name",
            value = uiState.title,
            onValueChange = onTitleChange,
            maxTextLen = 20,
        )
>>>>>>> origin/dev

        TextFieldApp(
            labelTitle = "Description",
            onValueChange = onDescriptionChange,
            singleLine = false,
            maxLines = 5,
            value = viewTask.description,
            style = AppText.CaptionRegular.copy(lineHeight = 20.sp, color = MaterialTheme.colorScheme.surface),
            maxTextLen = 500
        )

<<<<<<< HEAD
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
=======
        LabelRowContent(
            labelName = "Task Type: ",
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            if (canChangeTaskType) {
                var isTaskTypeExpanded by remember { mutableStateOf(false) }
>>>>>>> origin/dev
                val rotation by animateFloatAsState(
                    targetValue = if (!isTaskTypeExpanded) 180f else 0f,
                    label = "icon rotation"
                )

                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
<<<<<<< HEAD
                        Text(text = uiState.priority.toString(), style = AppText.Body2SemiBold)
=======
                        Text(text = uiState.taskType.name, style = AppText.CaptionRegular)
>>>>>>> origin/dev
                        ButtonApp(
                            onClick = { isTaskTypeExpanded = true },
                            type = ButtonType.TEXT,
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(0.dp, MaterialTheme.colorScheme.secondaryContainer),
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
                        MenuSelectionItem(
                            data = it,
                            selected = uiState.taskType == it,
                        )
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
                Text(text = uiState.taskType.name, style = AppText.CaptionRegular)
            }
        }

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
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
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

                ButtonApp(
                    onClick = onCreateTagClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    type = ButtonType.TEXT,
                ) {
                    Text("New", color = MaterialTheme.colorScheme.secondaryContainer)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(item.data.label.icon),
                            contentDescription = item.data.name,
                            tint = Color(item.data.color),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.data.name, color = MaterialTheme.colorScheme.onPrimary, style = AppText.BodyRegular)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_edit_pencil),
                            contentDescription = "Edit Tag",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    onEditTagClick(item.data)
                                }
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.icon_garbage),
                            contentDescription = "Delete Tag",
                            tint = Color.Red.copy(alpha = 0.9f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    onDeleteTagClick(item.data)
                                }
                        )
                        if (item.selected) {
                            Icon(
                                painter = painterResource(R.drawable.icon_done),
                                tint = MaterialTheme.colorScheme.primaryContainer,
                                contentDescription = "Selected",
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(16.dp)
                                    .background(MaterialTheme.colorScheme.onPrimary)
                            )
                        }
                    }
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
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_arrow_down),
<<<<<<< HEAD
                                contentDescription = null,
                                modifier = Modifier.rotate(rotation).size(16.dp),
                                tint = BlueGrey80
                            )
                        }
                    }
                    val priorityItems = TaskPriority.entries.map { priority ->
                        MenuSelectionItem(data = priority, selected = uiState.priority == priority)
=======
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
>>>>>>> origin/dev
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
<<<<<<< HEAD
                        Text(item.data.name)
=======
                        Text(
                            item.data.name,
                            style = if (item.selected) AppText.CaptionBold else AppText.CaptionRegular
                        )
>>>>>>> origin/dev
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

<<<<<<< HEAD
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
=======
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
>>>>>>> origin/dev
                            )
                        }
                    }

<<<<<<< HEAD
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
=======
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
>>>>>>> origin/dev
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
            TextFieldApp(
                modifier = Modifier.padding(horizontal = 20.dp),
                labelTitle = "Repetition",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { text ->
                    val repetition = text.filter { it.isDigit() }.toIntOrNull() ?: 0
                    onRepetitionChange(repetition)
                },
                singleLine = false,
                maxLines = 5,
                value = viewTask.repetition.toString(),
                style = AppText.CaptionRegular.copy(lineHeight = 20.sp, color = MaterialTheme.colorScheme.surface),
                maxTextLen = 3,
                visibleMaxText = false
            )
<<<<<<< HEAD
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
=======
>>>>>>> origin/dev
        }

//            Row(
//                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//            ) {
//                ButtonApp(
//                    onClick = { if (!isLoading) onCancel() },
//                    modifier = Modifier.weight(1f),
//                    type = ButtonType.OUTLINED,
//                ) {
//                    Text("Cancel")
//                }
//                ButtonApp(
//                    onClick = { if (!isLoading) onDone() },
//                    modifier = Modifier.weight(1f),
//                    type = ButtonType.FILLED,
//                ) {
//                    if (isLoading) {
//                        androidx.compose.material3.CircularProgressIndicator(
//                            modifier = Modifier.size(16.dp),
//                            color = MaterialTheme.colorScheme.primary,
//                            strokeWidth = 2.dp
//                        )
//                    } else {
//                        Text("Save")
//                    }
//                }
//            }
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
<<<<<<< HEAD
    LabelColumnContent(labelName = "Start & Due Date: ", iconId = R.drawable.icon_time, content = content, modifier = modifier)
=======
    LabelColumnContent(labelName = "Start: ", iconId = R.drawable.icon_time, content = content, modifier = modifier)
>>>>>>> origin/dev
}

private fun toLocalDate(timestamp: Long): LocalDate {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

<<<<<<< HEAD
private fun LocalDate.toEpochMillisAtStartOfDay(): Long {
    return atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
=======
fun LocalDate.toEpochMillisAtStartOfDay(): Long {
    return atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun TaskDetailActionUiState.toDomainTask(
    baseTask: Task,
    status: TaskStatus = baseTask.status,
    uuid: Uuid = baseTask.uuid,
): Task {
    val startDate = selectedStartDateMillis?.let(::toLocalDate) ?: baseTask.startDate
    val dueDate = selectedDueDateMillis?.let(::toLocalDate) ?: baseTask.dueDate
    val resolvedProjectId =
        if (taskType == TaskType.PROJECT) selectedProjectId ?: baseTask.projectId else null

    return baseTask.copy(
        uuid = uuid,
        title = title,
        description = description,
        type = taskType,
        status = status,
        priority = priority,
        tags = selectedTags,
        startDate = startDate,
        dueDate = dueDate,
        projectId = resolvedProjectId,
        repetition = repetition,
    )
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
                onTaskTypeChange = { newType -> uiState = uiState.copy(taskType = newType) },
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
                onCreateTagClick = {},
                onOpenDateRangePicker = {},
                onDone = {},
                onCancel = {},
                onRepetitionChange = { repetition -> uiState = uiState.copy(repetition = repetition) }
            )
        }
    }
>>>>>>> origin/dev
}