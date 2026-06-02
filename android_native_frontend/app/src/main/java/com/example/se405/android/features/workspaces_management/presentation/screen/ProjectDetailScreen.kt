@file:OptIn(ExperimentalUuidApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.se405.android.features.workspaces_management.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.AppHeader
import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.core.presentation.components.HabitLabel
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.chat_management.presentation.screen.ProjectChatRoom
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.projects
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.tags
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.workspaces_management.presentation.components.AddMembersPopUpContent
import com.example.se405.android.features.workspaces_management.presentation.components.AddableMember
import com.example.se405.android.features.workspaces_management.presentation.components.ProjectDetailCreateProjectTaskPopUp
import com.example.se405.android.features.workspaces_management.presentation.components.ProjectDetailKanbanSection
import com.example.se405.android.features.workspaces_management.presentation.components.ProjectDetailMemberSection
import com.example.se405.android.features.workspaces_management.presentation.components.ProjectDetailTaskSection
import com.example.se405.android.features.workspaces_management.presentation.components.SimpleProgressBar
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.ProjectDetailViewModel
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceUiEvent
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceUiState
import com.example.se405.android.graphql.GetProjectQuery
import com.example.se405.android.graphql.type.TaskStatus
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Composable
fun ProjectDetailRoute(
    viewModel: ProjectDetailViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onNavigateToTask: (Uuid, Uuid?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val addableMembersForProject by viewModel.addableMembersForProject.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

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
            CircularProgressIndicator()
        }
        is WorkspaceUiState.Success -> {
            ProjectDetailScreen(
                project = state.data,
                availableTags = availableTags,
                addableMembersForProject = { addableMembersForProject },
                isActionLoading = { (uiState as? WorkspaceUiState.Success)?.isActionLoading == true },
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                onCreateTask = { title, desc, priority, start, due, tagIds, assigneeId ->
                    viewModel.createTask(title, desc, priority, start, due, tagIds, assigneeId)
                },
                onCreateTag = { name, color, label, ownershipType ->
                    viewModel.createTag(name, color, label ?: BuiltinLabels.first(), ownershipType)
                },
                onBackClick = onBackClick,
                loadAddableMembersForProject = { viewModel.loadAddableMembersForProject() },
                addMembersToProject = { userIds -> viewModel.addMembersToProject(userIds) },
                onNavigateToTask = onNavigateToTask
            )
        }
        WorkspaceUiState.NotFound -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "Project not found", Toast.LENGTH_SHORT).show()
                onBackClick()
            }
        }
    }
}

private enum class ProjectTab {
    TASKS, MEMBERS, KANBAN, CHAT
}

@Composable
fun ProjectDetailScreen(
    project: GetProjectQuery.GetProject? = null,
    availableTags: List<Tag> = emptyList(),
    addableMembersForProject: () -> List<AddableMember> = { emptyList() },
    isActionLoading: () -> Boolean = { false },
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onCreateTask: (
        title: String,
        description: String,
        priority: TaskPriority,
        startDate: LocalDate?,
        dueDate: LocalDate?,
        tagIds: List<Uuid>,
        assigneeId: Uuid?,
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onCreateTag: (
        name: String,
        color: Int,
        label: HabitLabel?,
        ownershipType: TagOwnershipType,
    ) -> Unit = { _, _, _, _ -> },
    onBackClick: () -> Unit = {},
    loadAddableMembersForProject: () -> Unit = {},
    addMembersToProject: (List<Uuid>) -> Unit = {_ ->},
    onNavigateToTask: (Uuid, Uuid?) -> Unit = { _, _ -> }
) {
    var activeTab by remember { mutableStateOf(ProjectTab.KANBAN) }
    val availableTagsState = rememberUpdatedState(availableTags)
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                title = project?.let { "Project: ${it.name}" } ?: "Error project now found",
                showBackButton = true,
                onBackClick = onBackClick,
            )

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (project == null) {
                    Text(text = "Project not found", style = AppText.DisplayLight)
                    return
                }

                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
//                    AvatarRow(avatars = project.members.map { it.user?.avatarUrl ?: "" }, size = 32.dp) {
//                            avatarUrl -> ImageAvatar(imageUrl = avatarUrl, size = 32.dp * 0.75f)
//                    }
                    Text(project.let { "Project: ${it.name}" }, style = AppText.HeadBold)

                    val doneTasks = project.tasks.filter { it.status == TaskStatus.DONE }
                    val progress = doneTasks.size.toFloat() / project.tasks.size
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Progress (${(progress * 100).toInt()} %)", style = AppText.HeadBold, color = MaterialTheme.colorScheme.onPrimary)
                        Text(text = "${doneTasks.size} / ${project.tasks.size} tasks", style = AppText.HeadRegular)
                    }
                    SimpleProgressBar(progress = progress, modifier = Modifier.height(12.dp))
                }

                val tabs = listOf(ProjectTab.TASKS to "Tasks", ProjectTab.KANBAN to "Kanban", ProjectTab.MEMBERS to "Members", ProjectTab.CHAT to "Chat")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    tabs.forEach { (tab, title) ->
                        ProjectTabItem(
                            modifier = Modifier.weight(1f),
                            title = title,
                            isActive = activeTab == tab,
                            onClick = { activeTab = tab }
                        )
                    }
                }

                if (activeTab == ProjectTab.CHAT) {
                    ProjectChatRoom(
                        projectId = project.uuid,
                        projectName = project.name,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                } else {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        when (activeTab) {
                            ProjectTab.KANBAN -> ProjectDetailKanbanSection(orgTasks = project.tasks)
                            ProjectTab.TASKS -> ProjectDetailTaskSection(tasks = project.tasks,
                                onTaskClick = {task -> onNavigateToTask(Uuid.parse(task.uuid), task.projectId?.let { Uuid.parse(it) })})
                            ProjectTab.MEMBERS -> ProjectDetailMemberSection(project.members)
                            ProjectTab.CHAT -> Unit
                        }
                    }
                }
            }
        }

        val popupController = LocalPopupController.current
        if (activeTab != ProjectTab.CHAT) {
        FloatingActionButton(
            onClick = {
                popupController.push { onDismiss ->
                    when(activeTab) {
                        ProjectTab.KANBAN,
                        ProjectTab.TASKS -> ProjectDetailCreateProjectTaskPopUp(
                            projectName = project?.name ?: "",
                            projectMembers = project?.members?.mapNotNull { member ->
                                member.user?.let { user ->
                                    AddableMember(
                                        userId = Uuid.parse(user.uuid),
                                        displayName = user.displayName,
                                        username = user.username,
                                        avatarUrl = user.avatarUrl
                                    )
                                }
                            } ?: emptyList(),
                            availableTags = availableTagsState.value,
                            onCreate = { title, desc, priority, start, due, tagIds, assigneeId ->
                                onCreateTask(title, desc, when(priority) {
                                    com.example.se405.android.graphql.type.TaskPriority.HIGH -> TaskPriority.HIGH
                                    com.example.se405.android.graphql.type.TaskPriority.MEDIUM -> TaskPriority.MEDIUM
                                    else -> TaskPriority.LOW
                                }, start, due, tagIds, assigneeId)
                                onDismiss()
                            },
                            onCreateTag = { name, color, label, ownershipType ->
                                onCreateTag(name, color, label, ownershipType)
                            },
                            onCancel = onDismiss)

                        ProjectTab.MEMBERS -> {
                            LaunchedEffect(Unit) { loadAddableMembersForProject() }
                            AddProjectMembersPopUp(
                                projectName = project?.name ?: "",
                                candidates = addableMembersForProject(),
                                isLoading = isActionLoading(),   // uiState.isActionLoading
                                onAdd = { selectedIds ->
                                    addMembersToProject(selectedIds)
                                    onDismiss()
                                },
                                onCancel = onDismiss,
                            )
                        }
                        ProjectTab.CHAT -> Unit
                    }
                }
            },
            modifier = Modifier.align(alignment = Alignment.BottomEnd).padding(24.dp),
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
}

// ─── Wrapper cho ProjectDetailScreen ─────────────────────────────────────────

/**
 * Popup thêm members vào project.
 *
 * candidates = workspace members chưa là project member (tính từ VM).
 */
@OptIn(ExperimentalUuidApi::class)
@Composable
fun AddProjectMembersPopUp(
    projectName: String,
    candidates: List<AddableMember>,
    isLoading: Boolean = false,
    onAdd: (List<Uuid>) -> Unit,
    onCancel: () -> Unit,
) {
    AddMembersPopUpContent(
        title = "Add members to project",
        subtitle = projectName,
        candidates = candidates,
        isLoading = isLoading,
        onAdd = onAdd,
        onCancel = onCancel,
    )
}


fun Project.toGetProjectQueryTask(): GetProjectQuery.GetProject {
    return GetProjectQuery.GetProject(
        uuid = this.id.toString(),
        name = this.name,
        workspaceId = "",
        members = this.member.map {m ->
            GetProjectQuery.Member(
                user = GetProjectQuery.User(
                    uuid = m.user?.uuid.toString(),
                    displayName = m.user?.displayName ?: "unknown",
                    username = m.user?.username ?: "unknown",
                    avatarUrl = m.user?.avatarUrl,
                    email = m.user?.email ?: "unknown@email.com",
                ),
                joinedAt = m.joinedAt.toString(),
            )
        },
        createdAt = this.createdAt.toString(),
        tasks = this.tasks.map { tsk ->
            GetProjectQuery.Task(
                uuid = tsk.uuid.toString(),
                title = tsk.title,
                type = when (tsk.type) {
                    TaskType.HABIT -> com.example.se405.android.graphql.type.TaskType.HABIT
                    TaskType.PROJECT -> com.example.se405.android.graphql.type.TaskType.PROJECT
                },
                description = tsk.description,
                startDate = tsk.startDate.toString(),
                priority = when (tsk.priority) {
                    TaskPriority.HIGH -> com.example.se405.android.graphql.type.TaskPriority.HIGH
                    TaskPriority.MEDIUM -> com.example.se405.android.graphql.type.TaskPriority.MEDIUM
                    TaskPriority.LOW -> com.example.se405.android.graphql.type.TaskPriority.LOW

                },
                dueDate = tsk.dueDate.toString(),
                creator = GetProjectQuery.Creator(
                    uuid = tsk.creator?.uuid.toString(),
                    displayName = tsk.creator?.displayName ?: "unknown",
                    avatarUrl = tsk.creator?.avatarUrl
                ),
                repetition = tsk.repetition,
                projectId = tsk.projectId.toString(),
                status = when (tsk.status) {
                    com.example.se405.android.features.tasks_management.domain.entity.TaskStatus.DONE -> TaskStatus.DONE
                    com.example.se405.android.features.tasks_management.domain.entity.TaskStatus.TODO -> TaskStatus.TODO
                    com.example.se405.android.features.tasks_management.domain.entity.TaskStatus.FAILED -> TaskStatus.FAILED
                    else -> TaskStatus.TODO
                },
                taskCompletionLogs = tsk.taskCompletionLog.map { log ->
                    GetProjectQuery.TaskCompletionLog(
                        userId = log.userId.toString(),
                        taskCompletionId = log.taskCompletionId.toString(),
                        status = log.status.toString(),
                        completedAt = log.completedAt.toString(),
                        date = log.date.toString()
                    )
                },
                assignees = emptyList(),
                tags = emptyList(),
            )
        }
    )
}

@Composable
private fun ProjectTabItem(
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

    val textStyle = if (isActive) {
        AppText.Body2Light
    } else {
        AppText.Body2SemiBold
    }

    Column(
        modifier = modifier.clip(RoundedCornerShape(8.dp))
            .background(if (isActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = textColor,
            style = textStyle
        )
    }
}

@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
private fun ProjectDetailScreenPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()){
        Android_Theme {
            ProjectDetailScreen(project = projects.first().toGetProjectQueryTask(), availableTags = tags, onCreateTask = { _, _, _, _, _, _, _ -> },  onCreateTag = { _, _, _, _ -> },  onNavigateToTask = { _, _ -> }, onBackClick =  {})
        }
    }
}
