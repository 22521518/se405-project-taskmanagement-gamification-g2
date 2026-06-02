@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskDetailViewModel
import com.example.se405.android.features.workspaces_management.presentation.components.AvatarItem
import com.example.se405.android.features.workspaces_management.presentation.components.AvatarRow
import com.example.se405.android.features.workspaces_management.presentation.components.SubScreenHeader
import com.example.se405.android.features.workspaces_management.presentation.components.TagChip
import com.example.se405.android.features.workspaces_management.presentation.components.TagChipUi
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceUiEvent
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceUiState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun TaskDetailRoute(
    viewModel: TaskDetailViewModel,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val projectName by viewModel.projectName.collectAsStateWithLifecycle()
    val workspaceName by viewModel.workspaceName.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is WorkspaceUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    when (val state = uiState) {
        WorkspaceUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is WorkspaceUiState.Success -> {
            TaskDetailScreen(
                task = state.data.task,
                projectName = projectName,
                workspaceName = workspaceName,
                canDelete = state.data.canDelete,
                canCompleteOrFail = state.data.canCompleteOrFail,
                onDoneClick = { viewModel.markTaskDone() },
                onCancelClick = { viewModel.markTaskWontDo() },
                onDeleteClick = {
                    viewModel.deleteTask(onSuccess = onBackClick)
                },
                onBackClick = onBackClick
            )
        }
        WorkspaceUiState.NotFound -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "Task not found", Toast.LENGTH_SHORT).show()
                onBackClick()
            }
        }
    }
}

@Composable
fun TaskDetailScreen(
    task: Task,
    projectName: String?,
    workspaceName: String?,
    canDelete: Boolean,
    canCompleteOrFail: Boolean,
    onDoneClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SubScreenHeader {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.clickable { onBackClick() }) {
                        Icon(
                            painter = painterResource(R.drawable.icon_arrow_left),
                            modifier = Modifier.size(18.dp),
                            contentDescription = "Go Back"
                        )
                    }
                    Text(
                        text = "Task Detail",
                        style = AppText.HeadBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusText = task.status.name.toDisplayText()
                    Text(
                        text = statusText,
                        style = AppText.CaptionBold,
                        color = Color(0xFF166534),
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color(0xFF22C55E),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .background(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    val priorityStr = task.priority.name
                    val (priorityBg, priorityBorder, priorityText) = when (priorityStr.toDisplayText()) {
                        "HIGH".toDisplayText() -> Triple(Color(0xFFFEE2E2), Color(0xFFEF4444), Color(0xFFB91C1C))
                        "MEDIUM".toDisplayText() -> Triple(Color(0xFFFEF3C7), Color(0xFFF59E0B), Color(0xFF92400E))
                        else -> Triple(Color(0xFFDBEAFE), Color(0xFF3B82F6), Color(0xFF1D4ED8))
                    }

                    Text(
                        text = priorityStr,
                        style = AppText.CaptionBold,
                        color = priorityText,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = priorityBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .background(color = priorityBg, shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                Text(
                    text = task.title,
                    style = AppText.DisplayBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (task.type == TaskType.PROJECT && projectName != null) {
                    Text(
                        text = "Project: $projectName" + (if (workspaceName != null) " ($workspaceName)" else ""),
                        style = AppText.BodyRegular,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = task.description.ifBlank { "No description provided." },
                    style = AppText.Body2Regular,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp),
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (task.type == TaskType.PROJECT) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "Assignees: ", style = AppText.BodyRegular)
                            if (task.assignees.isEmpty()) {
                                Text("No assignees", style = AppText.Body2Regular, color = Color.Gray)
                            } else {
                                AvatarRow(avatars = task.assignees.map { AvatarItem(it.avatarUrl, it.displayName ?: "") })
                            }
                        }
                        task.dueDate?.let { dueDate ->
                            Text("Due date: $dueDate", style = AppText.BodyRegular)
                        }
                    }
                } else {
                    val completedCount = task.taskCompletionLog.count { it.status == TaskStatus.DONE }
                    Text(
                        text = "Repetition: $completedCount / ${if (task.repetition <= 0) 1 else task.repetition}",
                        style = AppText.BodyRegular
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Tags: ", style = AppText.BodyRegular)
                    if (task.tags.isEmpty()) {
                        Text(
                            text = "No tags available.",
                            style = AppText.Body2Regular,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            task.tags.forEach { tag ->
                                TagChip(
                                    tag = TagChipUi(color = tag.color, name = tag.name, icon = tag.label.icon),
                                    isSelected = false,
                                    enabled = true,
                                    onClick = {}
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Actions Section
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (canCompleteOrFail) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ButtonApp(
                                onClick = onDoneClick,
                                modifier = Modifier.weight(1f),
                                type = ButtonType.FILLED
                            ) {
                                Text("Done", style = AppText.BodyBold, color = Color.White)
                            }
                            ButtonApp(
                                onClick = onCancelClick,
                                modifier = Modifier.weight(1f),
                                type = ButtonType.OUTLINED
                            ) {
                                Text("Cancel", style = AppText.BodyBold, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                    if (canDelete) {
                        ButtonApp(
                            onClick = onDeleteClick,
                            modifier = Modifier.fillMaxWidth(),
                            type = ButtonType.OUTLINED,
                            border = BorderStroke(2.dp, Color(0xFFEF4444))
                        ) {
                            Text("Remove", style = AppText.BodyBold, color = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
private fun TaskDetailScreenPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()){
        Android_Theme {
            val baseTask = PreviewDomainEntityData.tasks.first()
            TaskDetailScreen(
                task = baseTask,
                projectName = "Project Name",
                workspaceName = "Workspace Name",
                canDelete = true,
                canCompleteOrFail = true
            )
        }
    }
}

private fun String.toDisplayText(): String =
    lowercase()
        .split("_")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }