@file:OptIn(ExperimentalUuidApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.se405.android.features.workspaces_management.presentation.screen

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.AppHeader
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import com.example.se405.android.features.workspaces_management.presentation.components.IconSearchBar
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceManagementViewModel
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceUiEvent
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceUiState
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Composable
fun WorkspaceManagementRoute(
    viewModel: WorkspaceManagementViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onNavigateToWorkspace: (Uuid) -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
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
            WorkspaceManagementScreen(
                workspaces = state.data,
                searchQuery = searchQuery,
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                onNewWorkspaceCreate = { workspaceName ->
                    viewModel.createWorkspace(workspaceName)
                },
                onBackClick = onBackClick,
                onNavigateToWorkspace = onNavigateToWorkspace
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

@Composable
fun WorkspaceManagementScreen(
    workspaces: List<Workspace>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onNewWorkspaceCreate: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onNavigateToWorkspace: (Uuid) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(title = "Your workspaces")
            IconSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
            )
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (workspace in workspaces) {
                        WorkspaceCard(workspace = workspace, modifier = Modifier.clickable(onClick = { onNavigateToWorkspace(workspace.id) }))
                    }
                }
            }
        }
        val popupController = LocalPopupController.current
        FloatingActionButton(
            onClick = {
                popupController.push { onDismiss ->
                    CreateWorkspacePopUp(
                        onCreate = { workspaceName ->
                            onNewWorkspaceCreate(workspaceName)
                            onDismiss() },
                        onCancel = { onDismiss() }
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
                contentDescription = "Add Workspace",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
private fun WorkspaceCard(
    modifier: Modifier = Modifier,
    workspace: Workspace,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column (modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = workspace.name,
                    style = AppText.HeadSemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                val yourRole = workspace.members.find { it.userId == workspace.members[0].userId }?.role
                Text(
                    text = yourRole?.name ?: "Member",
                    style = AppText.CaptionBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(end = 8.dp, bottom = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Icon(
                        painter = painterResource(R.drawable.icon_people),
                        contentDescription = "Members",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = " ${workspace.members.size} Members",
                        color = Color.Black,
                        style = AppText.Body2SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Icon(
                        painter = painterResource(R.drawable.icon_work_project),
                        contentDescription = "Projects",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "${workspace.projects.size} projects",
                        color = Color.Black,
                        style = AppText.Body2SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
                    )
                }
            }
            Text("Recents projects", style = AppText.Body2Regular, color = Color.Black)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                workspace.projects.take(4).forEach { project ->
                    Text(
                        text = project.name,
                        style = AppText.Body2Regular,
                        modifier = Modifier
                            .padding(end = 8.dp, bottom = 8.dp)
                            .border(
                                width = 4.dp,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

sealed interface CreateWorkspaceUiState {
    data class Editing(val workspaceName: String = "", val isError: Boolean = false) : CreateWorkspaceUiState
    object Loading : CreateWorkspaceUiState
}
@Composable
fun CreateWorkspacePopUp(
    onCreate: (String) -> Unit = {},
    onCancel: () -> Unit = {},
) {
    var uiState by remember { mutableStateOf<CreateWorkspaceUiState>(CreateWorkspaceUiState.Editing()) }
    PopUpLayout {
        if (uiState is CreateWorkspaceUiState.Loading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val currentState =
                uiState as? CreateWorkspaceUiState.Editing ?: CreateWorkspaceUiState.Editing()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Create new workspace", style = AppText.DisplayBold)
                    Text(
                        text = "Workspaces help you organize projects and members in one shared space.",
                        style = AppText.Body2Regular,
                    )
                }
                Column() {
                    Text(text = "Workspace name*", style = AppText.BodyBold)
                    OutlinedTextField(
                        value = currentState.workspaceName,
                        onValueChange = { newValue ->
                            uiState = CreateWorkspaceUiState.Editing(
                                workspaceName = newValue,
                                isError = false
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        isError = currentState.isError,
                        supportingText = {
                            if (currentState.isError) {
                                Text(text = "Workspace name cannot be blank", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ButtonApp(onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        type = ButtonType.OUTLINED) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    ButtonApp(
                        type = ButtonType.FILLED,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (currentState.workspaceName.isBlank()) {
                                uiState = currentState.copy(isError = true)
                            } else {
                                uiState = CreateWorkspaceUiState.Loading
                                onCreate(currentState.workspaceName)
                            }
                        }
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xfff, showBackground = true, name = "WorkspaceManagementScreen")
@Composable
fun WorkspaceManagementScreenPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()) {
        Android_Theme {
            WorkspaceManagementScreen(PreviewDomainEntityData.workspaces, "", {})
//            CreateWorkspacePopUp()
        }
    }
}