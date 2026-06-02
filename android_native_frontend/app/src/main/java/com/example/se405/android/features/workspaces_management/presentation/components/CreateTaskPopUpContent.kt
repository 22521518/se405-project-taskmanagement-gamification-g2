@file:Suppress("AssignedValueIsNeverRead")
@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.HabitLabel
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.*
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.projects
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.tags
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.workspaces_management.presentation.validation.CreateTaskInputError
import com.example.se405.android.features.workspaces_management.presentation.validation.CreateTaskInputValidator
import com.example.se405.android.graphql.type.TaskPriority
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun CreateTaskProjectPopUpContent(
    availableTags: List<Tag> = emptyList(),
    projectMembers: List<AddableMember> = emptyList(),
    isProjectSelected: Boolean = true,
    onCreate: (
        title: String,
        description: String,
        priority: TaskPriority,
        startDate: LocalDate?,
        dueDate: LocalDate?,
        tagIds: List<Uuid>,
        assigneeId: Uuid?,
    ) -> Unit,
    onCreateTag: (name: String, color: Int, label: HabitLabel, ownershipType: TagOwnershipType) -> Unit,
    onCancel: () -> Unit,
    projectSelector: @Composable () -> Unit
) {
    var uiState by remember { mutableStateOf<CreateTaskUiState>(CreateTaskUiState.Editing()) }
    val popupController = LocalPopupController.current

    LaunchedEffect(projectMembers) {
        val state = uiState as? CreateTaskUiState.Editing ?: return@LaunchedEffect
        if (state.selectedAssigneeId != null && projectMembers.none { it.userId == state.selectedAssigneeId }) {
            uiState = state.copy(selectedAssigneeId = null)
        }
    }
    var tagSheetState by remember { mutableStateOf<CreateTagSheetState>(CreateTagSheetState.Hidden) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val dueDatePickerState = rememberDatePickerState()

    if (tagSheetState !is CreateTagSheetState.Hidden) {
        CreateTagBottomSheet(
            state = tagSheetState,
            onStateChange = { tagSheetState = it },
            onConfirm = { name, color, label ->
                tagSheetState = CreateTagSheetState.Loading
                onCreateTag(name, color, label, TagOwnershipType.WORKSPACE)
                tagSheetState = CreateTagSheetState.Hidden
            },
            onDismiss = { tagSheetState = CreateTagSheetState.Hidden },
        )
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / 86_400_000)
                        val editing = uiState as? CreateTaskUiState.Editing ?: return@TextButton
                        uiState = editing.copy(startDate = date, isDateRangeError = false)
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = startDatePickerState) }
    }

    if (showDueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDatePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / 86_400_000)
                        val editing = uiState as? CreateTaskUiState.Editing ?: return@TextButton
                        uiState = editing.copy(dueDate = date, isDateRangeError = false)
                    }
                    showDueDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDueDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dueDatePickerState) }
    }

    PopUpLayout {
        when (uiState) {
            is CreateTaskUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is CreateTaskUiState.Editing -> {
                val state = uiState as CreateTaskUiState.Editing
                val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Create new task", style = AppText.DisplayBold)
                        Text(text = "Add a task to track progress.", style = AppText.Body2Regular)
                    }

                    projectSelector()
                    AnimatedVisibility(visible = state.isProjectError) {
                        Text(
                            text = "Please select a project",
                            color = MaterialTheme.colorScheme.error,
                            style = AppText.Body2Regular
                        )
                    }

                    // Title
                    Column {
                        Text(text = "Task title*", style = AppText.BodyBold)
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = { uiState = state.copy(title = it, isTitleError = false) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            placeholder = { Text("Enter task title") },
                            isError = state.isTitleError,
                            supportingText = {
                                if (state.isTitleError) {
                                    Text(text = "Task title cannot be blank", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            singleLine = true,
                        )
                    }

                    // Description
                    Column {
                        Text(text = "Description", style = AppText.BodyBold)
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { uiState = state.copy(description = it) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 80.dp),
                            placeholder = { Text("Optional description…") },
                            maxLines = 4,
                        )
                    }

                    // Priority
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Priority", style = AppText.BodyBold)
                        PrioritySelector(selected = state.priority, onSelect = { uiState = state.copy(priority = it) })
                    }

                    // Date range
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Date range", style = AppText.BodyBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DateChip(
                                label = "Start",
                                date = state.startDate?.format(dateFormatter),
                                modifier = Modifier.weight(1f),
                                isError = state.isDateRangeError,
                                onClick = { showStartDatePicker = true }
                            )
                            DateChip(
                                label = "Due",
                                date = state.dueDate?.format(dateFormatter),
                                modifier = Modifier.weight(1f),
                                isError = state.isDateRangeError,
                                onClick = { showDueDatePicker = true }
                            )
                        }
                        AnimatedVisibility(visible = state.isDateRangeError) {
                            Text(text = "Due date must be after start date", color = MaterialTheme.colorScheme.error, style = AppText.Body2Regular)
                        }
                    }

                    // Assignee
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Assignee", style = AppText.BodyBold)
                        val selectedMember = projectMembers.find { it.userId == state.selectedAssigneeId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Blue90)
                                .border(1.dp, BlueGrey80, RoundedCornerShape(8.dp))
                                .clickable {
                                    popupController.push { onDismissAssigneePopUp ->
                                        SelectAssigneePopUpContent(
                                            title = "Assign task to...",
                                            subtitle = "Select a project member",
                                            candidates = projectMembers,
                                            selectedAssigneeId = state.selectedAssigneeId,
                                            onSelect = { assigneeId ->
                                                uiState = state.copy(selectedAssigneeId = assigneeId)
                                                onDismissAssigneePopUp()
                                            },
                                            onCancel = { onDismissAssigneePopUp() }
                                        )
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedMember != null) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Blue40),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedMember.displayName.firstOrNull()?.uppercase() ?: "?",
                                        style = AppText.BodyBold,
                                        color = Color.White
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = selectedMember.displayName, style = AppText.BodySemiBold, color = Color.Black)
                                    Text(text = "@${selectedMember.username}", style = AppText.Body2Regular, color = Color.DarkGray)
                                }
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            uiState = state.copy(selectedAssigneeId = null)
                                        }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Assignee",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.DarkGray
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.DarkGray
                                )
                                Text(
                                    text = "Unassigned",
                                    style = AppText.BodySemiBold,
                                    color = Color.DarkGray,
                                    modifier = Modifier.weight(1f)
                                )
                            }
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
                            TextButton(
                                onClick = { tagSheetState = CreateTagSheetState.Editing() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Create tag", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                                Spacer(Modifier.width(4.dp))
                                Text("New tag", style = AppText.Body2Regular, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }

                        if (availableTags.isEmpty()) {
                            Text(text = "No tags available. Create one above.", style = AppText.Body2Regular, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(availableTags) { tag ->
                                    val isSelected = tag.uuid in state.selectedTagIds
                                    val canSelect = isSelected || state.selectedTagIds.size < DEFAULT_MAX_TAGS
                                    TagChip(
                                        tag = TagChipUi(color = tag.color, name = tag.name),
                                        isSelected = isSelected,
                                        enabled = canSelect,
                                        onClick = {
                                            uiState = if (isSelected) {
                                                state.copy(selectedTagIds = state.selectedTagIds - tag.uuid)
                                            } else {
                                                state.copy(selectedTagIds = state.selectedTagIds + tag.uuid)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ButtonApp(onClick = onCancel, modifier = Modifier.weight(1f), type = ButtonType.OUTLINED) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(24.dp))
                        ButtonApp(
                            type = ButtonType.FILLED,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val result = CreateTaskInputValidator.validate(
                                    title = state.title,
                                    description = state.description,
                                    projectSelected = isProjectSelected,
                                    startDate = state.startDate,
                                    dueDate = state.dueDate,
                                    tagIds = state.selectedTagIds,
                                )
                                uiState = when (result) {
                                    is CreateTaskInputValidator.Result.Valid -> {
                                        onCreate(result.title, result.description, state.priority, state.startDate, state.dueDate, state.selectedTagIds, state.selectedAssigneeId)
                                        CreateTaskUiState.Loading
                                    }
                                    is CreateTaskInputValidator.Result.Invalid -> when (result.error) {
                                        CreateTaskInputError.BlankTitle -> state.copy(isTitleError = true, isProjectError = false, isDateRangeError = false)
                                        CreateTaskInputError.ProjectRequired -> state.copy(isProjectError = true, isTitleError = false, isDateRangeError = false)
                                        CreateTaskInputError.InvalidDateRange -> state.copy(isDateRangeError = true, isTitleError = false, isProjectError = false)
                                        // Tag count/duplication is already prevented by the chip selector;
                                        // surface it on the title row as a defensive fallback.
                                        CreateTaskInputError.TooManyTags,
                                        CreateTaskInputError.DuplicateTags -> state
                                    }
                                }
                            }
                        ) { Text("Create") }
                    }
                }
            }
        }
    }
}

// ─── Priority selector ────────────────────────────────────────────────────────

@Composable
fun PrioritySelector(
    selected: TaskPriority,
    onSelect: (TaskPriority) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskPriority.knownEntries.forEach { priority ->
            val isSelected = priority == selected
            val (bgColor, contentColor, label) = when (priority) {
                TaskPriority.LOW -> Triple(Blue90, Blue60, "Low")
                TaskPriority.MEDIUM -> Triple(Blue80, Blue40, "Medium")
                TaskPriority.HIGH -> Triple(Blue40, White, "High")
                else -> Triple(Blue90, Blue60, priority.rawValue)
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

// ─── Date chip ────────────────────────────────────────────────────────────────

@Composable
fun DateChip(
    label: String,
    date: String?,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onClick: () -> Unit,
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        date != null -> Blue40
        else -> BlueGrey80
    }
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (date != null)  Color.Transparent else Blue90,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (date != null) Color.Black else Color.DarkGray,
            )
            Column {
                Text(text = label, style = AppText.Body2Regular, color = Color.Black)
                Text(
                    text = date ?: "Pick date",
                    style = AppText.BodySemiBold,
                    color = if (date != null) Color.Black else Color.DarkGray,
                )
            }
        }
    }
}

// ─── Tag chip ─────────────────────────────────────────────────────────────────

data class TagChipUi(
    val color: Int,
    val name: String,
    val icon: Int? = null,
)

@Composable
fun TagChip(
    tag: TagChipUi,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val tagColor = Color(tag.color)
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) tagColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.5.dp,
            color = if (isSelected) tagColor else BlueGrey80.copy(alpha = if (enabled) 1f else 0.4f)
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if(tag.icon != null) {
                Icon(painter = painterResource(id = tag.icon), tint = tagColor, contentDescription = null)
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(tagColor.copy(alpha = if (enabled) 1f else 0.4f))
                )
            }
            Text(
                text = tag.name,
                style = AppText.BodyBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = tagColor,
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
fun CreateTaskProjectPopUpContentPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()){
        Android_Theme {
            CreateTaskProjectPopUpContent(
                availableTags = tags,
                projectMembers = emptyList(),
                onCreate = { _, _, _, _, _, _, _ -> },
                onCreateTag = { _, _, _, _ -> },
                onCancel = {},
                projectSelector = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Project: ", style = AppText.BodyBold)
                        Text(
                            text = projects.first().name,
                            style = AppText.BodySemiBold.copy(fontStyle = FontStyle.Italic)
                        )
                    }
                }
            )
        }
    }
}
