@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.core.presentation.components.HabitLabel
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import com.example.se405.android.features.workspaces_management.presentation.components.AddMembersPopUpContent
import com.example.se405.android.features.workspaces_management.presentation.components.AddableMember
import com.example.se405.android.features.workspaces_management.presentation.components.WorkspaceDetailCreateProjectPopUp
import com.example.se405.android.features.workspaces_management.presentation.components.SimpleProgressBar
import com.example.se405.android.features.workspaces_management.presentation.components.SubScreenHeader
import com.example.se405.android.features.workspaces_management.presentation.components.WorkspaceDetailCreateWorkspaceTaskPopUp
import com.example.se405.android.features.workspaces_management.presentation.components.WorkspaceDetailMemberSection
import com.example.se405.android.features.workspaces_management.presentation.components.WorkspaceDetailTaskSection
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceDetailViewModel
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceUiEvent
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceUiState
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun WorkspaceDetailRoute(
    viewModel: WorkspaceDetailViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onNavigateToProject: (Uuid) -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val addableMembersForWorkspace by viewModel.addableMembersForWorkspace.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
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
            CircularProgressIndicator()
        }
        is WorkspaceUiState.Success -> {
            WorkspaceDetailScreen(
                workspace = state.data.workspace,
                recentActivities = state.data.recentActivities,
                addableMembersForWorkspace = addableMembersForWorkspace,
                isActionLoading = state.isActionLoading,
                onBackClick = onBackClick,
                onNavigateToProject = onNavigateToProject,
                onNewProjectCreate = { projectName -> viewModel.createProject(projectName) },
                availableTags = availableTags,
                onCreateTask = { projectId, title, desc, priority, start, due, tagIds, assigneeId ->
                    viewModel.createTask(projectId, title, desc, priority, start, due, tagIds, assigneeId)
                               },
                onCreateTag = { name, color, label, ownershipType ->
                    viewModel.createTag(name, color, label ?: BuiltinLabels.first(), ownershipType)
                },
                loadAddableMembersForWorkspace = { viewModel.loadAddableMembersForWorkspace() },
                addMembersToWorkspace = { userIds -> viewModel.addMembersToWorkspace(userIds) },
            )
        }
        WorkspaceUiState.NotFound -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "Workspace not found", Toast.LENGTH_SHORT).show()
                onBackClick()
            }
        }
    }
}

private enum class WorkspaceTab {
    PROJECTS, TASKS, MEMBERS
}

@Composable
fun WorkspaceDetailScreen(
    workspace: Workspace,
    recentActivities: List<TaskCompletionLog> = emptyList(),
    availableTags: List<Tag>,
    addableMembersForWorkspace: List<AddableMember> = emptyList(),
    isActionLoading: Boolean = false,
    onCreateTask: (
        projectId: Uuid,
        title: String,
        description: String,
        priority: TaskPriority,
        startDate: LocalDate?,
        dueDate: LocalDate?,
        tagIds: List<Uuid>,
        assigneeId: Uuid?,
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onCreateTag: (
        name: String,
        color: Int,
        label: HabitLabel?,
        ownershipType: TagOwnershipType,
    ) -> Unit = { _, _, _, _ -> },
    onBackClick: () -> Unit = {},
    loadAddableMembersForWorkspace: () -> Unit = {},
    addMembersToWorkspace: (List<Uuid>) -> Unit = {_ ->},
    onNavigateToProject: (Uuid) -> Unit = {},
    onNewProjectCreate: (String) -> Unit = {},
) {
    var activeTab by remember { mutableStateOf(WorkspaceTab.TASKS) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SubScreenHeader {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { onBackClick() }) {
                        Icon(
                            painter = painterResource(R.drawable.icon_arrow_left),
                            modifier = Modifier.size(18.dp),
                            contentDescription = "Go Back"
                        )
                    }
                    Text(
                        text = workspace.name,
                        style = AppText.HeadBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val isProjectsActive = activeTab == WorkspaceTab.PROJECTS
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isProjectsActive) MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = 0.15f
                            ) else Color.Transparent
                        )
                        .clickable { activeTab = WorkspaceTab.PROJECTS }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val textColor =
                        if (isProjectsActive) MaterialTheme.colorScheme.onPrimary else Color.Gray
                    val textStyle = if (isProjectsActive) AppText.BodyBold else AppText.BodySemiBold

                    Text("${workspace.projects.size}", color = textColor, style = textStyle)
                    Text(
                        "Projects",
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        color = textColor
                    )
                }

                val isTasksActive = activeTab == WorkspaceTab.TASKS
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isTasksActive) MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = 0.15f
                            ) else Color.Transparent
                        )
                        .clickable { activeTab = WorkspaceTab.TASKS }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val textColor =
                        if (isTasksActive) MaterialTheme.colorScheme.onPrimary else Color.Gray.copy(
                            alpha = 0.6f
                        )
                    val textStyle = if (isTasksActive) AppText.BodyBold else AppText.BodySemiBold

                    Text(
                        "${workspace.projects.sumOf { it.tasks.size }}",
                        color = textColor,
                        style = textStyle
                    )
                    Text(
                        "Tasks",
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        color = textColor
                    )
                }

                val isMembersActive = activeTab == WorkspaceTab.MEMBERS
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isMembersActive) MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = 0.15f
                            ) else Color.Transparent
                        )
                        .clickable { activeTab = WorkspaceTab.MEMBERS }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val textColor =
                        if (isMembersActive) MaterialTheme.colorScheme.onPrimary else Color.Gray.copy(
                            alpha = 0.6f
                        )
                    val textStyle = if (isMembersActive) AppText.BodyBold else AppText.BodySemiBold

                    Text("${workspace.members.size}", color = textColor, style = textStyle)
                    Text(
                        "Members",
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        color = textColor
                    )
                }
            }

            when (activeTab) {
                WorkspaceTab.PROJECTS -> WorkspaceDetailProjectsSection(
                    projects = workspace.projects,
                    onNavigateToProject = onNavigateToProject
                )

                WorkspaceTab.TASKS -> WorkspaceDetailTaskSection(recentActivities, workspace.projects)
                WorkspaceTab.MEMBERS -> WorkspaceDetailMemberSection(workspace.members)
            }
        }

        val popupController = LocalPopupController.current
        FloatingActionButton(
            onClick = {
                popupController.push { onDismiss ->
                    when (activeTab) {
                        WorkspaceTab.PROJECTS -> WorkspaceDetailCreateProjectPopUp(
                            workspaceName = workspace.name,
                            onCreate = { projectName ->
                                onNewProjectCreate(projectName)
                                onDismiss() },
                            onCancel = { onDismiss() })
                         WorkspaceTab.MEMBERS -> {
                             LaunchedEffect(Unit) { loadAddableMembersForWorkspace() }
                             AddWorkspaceMembersPopUp(
                                 workspaceName = workspace.name,
                                 candidates = addableMembersForWorkspace,
                                 isLoading = isActionLoading,
                                 onAdd = { selectedIds ->
                                     addMembersToWorkspace(selectedIds)
                                     onDismiss()
                                 },
                                 onCancel = onDismiss,
                             )
                         }
                         WorkspaceTab.TASKS -> WorkspaceDetailCreateWorkspaceTaskPopUp(
                            availableProjects = workspace.projects,
                            availableTags = availableTags,
                            onCreate = { projectId, title, desc, priority, start, due, tagIds, assigneeId ->
                                onCreateTask(projectId, title, desc, when(priority) {
                                    com.example.se405.android.graphql.type.TaskPriority.HIGH -> TaskPriority.HIGH
                                    com.example.se405.android.graphql.type.TaskPriority.MEDIUM -> TaskPriority.MEDIUM
                                    else -> TaskPriority.LOW
                                }, start, due, tagIds, assigneeId)
                                onDismiss()
                            },
                            onCreateTag = { name, color, label, ownershipType ->
                                onCreateTag(name, color, label, ownershipType)
                            },
                            onCancel = onDismiss
                        )
                    }
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
                contentDescription = "Add Workspace",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun WorkspaceDetailProjectsSection(
    projects: List<Project>,
    onNavigateToProject: (Uuid) -> Unit
) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(projects.size) {
            ProjectCard(project = projects[it], onNavigateToProject = onNavigateToProject)
        }
    }
}

@Composable
private fun ProjectCard(
    modifier: Modifier = Modifier,
    project: Project,
    onNavigateToProject: (Uuid) -> Unit
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp)
        )
        .padding(16.dp)
        .clickable { onNavigateToProject(project.id) },
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = project.name, style = AppText.HeadBold)
        val progress = project.tasks.filter { it.status == TaskStatus.DONE }.size.toFloat() / project.tasks.size
        Text(text = "${project.tasks.size} tasks (${(progress * 100).toInt()} %)", style = AppText.Body2Bold, color = Color.Gray)
        SimpleProgressBar(progress = progress)
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "${project.member.size} members", style = AppText.Body2Bold)
        }
    }
}

// ─── Wrapper cho WorkspaceDetailScreen ───────────────────────────────────────

/**
 * Popup thêm members vào workspace.
 *
 * candidates = users chưa là workspace member (getUsersExcluding đã lọc từ VM).
 */
@OptIn(ExperimentalUuidApi::class)
@Composable
fun AddWorkspaceMembersPopUp(
    workspaceName: String,
    candidates: List<AddableMember>,
    isLoading: Boolean = false,
    onAdd: (List<Uuid>) -> Unit,
    onCancel: () -> Unit,
) {
    AddMembersPopUpContent(
        title = "Add members to workspace",
        subtitle = workspaceName,
        candidates = candidates,
        isLoading = isLoading,
        onAdd = onAdd,
        onCancel = onCancel,
    )
}

@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
fun WorkspaceDetailScreenPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()) {
        Android_Theme {
//            CreateProjectPopUp(workspaceName = PreviewDomainEntityData.workspaces[0].name, onCreate = {}, onCancel = {})
            WorkspaceDetailScreen(workspace = PreviewDomainEntityData.workspaces[0],recentActivities = PreviewDomainEntityData.taskCompletionLogs, availableTags = emptyList(), onBackClick = {})
        }
    }
}