package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.components.HabitLabel
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.*
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.tags
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.workspaces
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.graphql.type.TaskPriority
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun WorkspaceDetailCreateWorkspaceTaskPopUp(
    availableProjects: List<com.example.se405.android.features.tasks_management.domain.entity.Project>,
    availableTags: List<Tag> = emptyList(),
    onCreate: (
        projectUuid: Uuid,
        title: String,
        description: String,
        priority: TaskPriority,
        startDate: LocalDate?,
        dueDate: LocalDate?,
        tagIds: List<Uuid>,
        assigneeId: Uuid?,
    ) -> Unit,
    onCreateTag: (name: String, color: Int, label: HabitLabel, ownershipType: TagOwnershipType) -> Unit = { _, _, _, _ -> },
    onCancel: () -> Unit = {},
) {
    var selectedProject by remember { mutableStateOf(availableProjects.firstOrNull()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val projectMembers = remember(selectedProject) {
        selectedProject?.member?.map { member ->
            AddableMember(
                userId = member.userId,
                displayName = member.user?.displayName ?: member.user?.username ?: member.userId.toString(),
                username = member.user?.username ?: "",
                avatarUrl = member.user?.avatarUrl
            )
        } ?: emptyList()
    }

    CreateTaskProjectPopUpContent(
        availableTags = availableTags,
        projectMembers = projectMembers,
        onCreate = { title, desc, priority, start, due, tags, assigneeId ->
            selectedProject?.let { project ->
                onCreate(project.id, title, desc, priority, start, due, tags, assigneeId)
            }
        },
        onCreateTag = onCreateTag,
        onCancel = onCancel,
        projectSelector = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Project*", style = AppText.BodyBold)

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedProject?.name ?: "Select a project",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                            enabled = true
                        ).fillMaxWidth(),
                        textStyle = AppText.BodySemiBold
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        availableProjects.forEach { project ->
                            DropdownMenuItem(
                                text = { Text(text = project.name, style = AppText.BodyRegular) },
                                onClick = {
                                    selectedProject = project
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}


@ExperimentalUuidApi
@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
fun WorkspaceDetailCreateWorkspaceTaskPopUpPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()){
        Android_Theme {
            WorkspaceDetailCreateWorkspaceTaskPopUp(
                availableProjects = workspaces.first().projects,
                availableTags = tags,
                onCreate = { _, _, _, _, _, _, _, _ -> },
                onCreateTag = { _, _, _, _ -> },
                onCancel = {}
            )
        }
    }
}

