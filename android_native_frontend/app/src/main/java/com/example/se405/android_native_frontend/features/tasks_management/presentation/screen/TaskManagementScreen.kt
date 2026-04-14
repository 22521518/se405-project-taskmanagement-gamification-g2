@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.se405.android_native_frontend.core.presentation.popup.LocalPopupController
import com.example.se405.android_native_frontend.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android_native_frontend.features.tasks_management.presentation.components.TaskDetailActionUiState
import com.example.se405.android_native_frontend.features.tasks_management.presentation.components.TaskDetailCreatePopUp
import com.example.se405.android_native_frontend.features.tasks_management.presentation.components.TaskDetailPopUp
import com.example.se405.android_native_frontend.features.tasks_management.presentation.components.TaskDetailEditPopUp
import com.example.se405.android_native_frontend.features.tasks_management.presentation.components.TaskGroup
import com.example.se405.android_native_frontend.features.tasks_management.presentation.components.toTree
import com.example.se405.android_native_frontend.features.tasks_management.presentation.viewmodel.TaskManagementViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun TaskManagementScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskManagementViewModel = koinViewModel()
) {
    val workspaces by viewModel.workspaces.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val members by viewModel.members.collectAsState()
    
    val popupController = LocalPopupController.current

    val treeRoots = remember(workspaces) {
        workspaces.map { it.toTree() }
    }

    TaskGroup(
        roots = treeRoots,
        modifier = modifier.fillMaxSize(),
        onAddTaskClick = {
             val baseTask = PreviewDomainEntityData.tasks[0]
             
             popupController.push { onDismiss ->
                 var uiState by remember {
                     mutableStateOf(TaskDetailActionUiState(
                         title = "",
                         taskType = baseTask.type,
                         selectedTags = emptyList(),
                         priority = baseTask.priority,
                         description = "",
                         selectedStartDateMillis = null,
                         selectedDueDateMillis = null,
                         isTagSelectorVisible = false,
                         selectedProjectId = null,
                         selectedAssigneeId = null
                     ))
                 }

                 TaskDetailCreatePopUp(
                     taskDraft = baseTask,
                     uiState = uiState,
                     availableTags = availableTags,
                     projectsInWorkspace = projects,
                     membersInWorkspace = members,
                     canChangeTaskType = true,
                     onTaskTypeChange = { uiState = uiState.copy(taskType = it) },
                     onTitleChange = { uiState = uiState.copy(title = it) },
                     onDescriptionChange = { uiState = uiState.copy(description = it) },
                     onPriorityChange = { uiState = uiState.copy(priority = it) },
                     onToggleTag = { tag -> 
                         val newTags = if (uiState.selectedTags.contains(tag)) {
                             uiState.selectedTags.filter { t -> t.uuid != tag.uuid }
                         } else {
                             uiState.selectedTags + tag
                         }
                         uiState = uiState.copy(selectedTags = newTags)
                     },
                     onProjectSelected = { uiState = uiState.copy(selectedProjectId = it) },
                     onAssigneeSelected = { uiState = uiState.copy(selectedAssigneeId = it) },
                     onTagSelectorVisibilityChange = { uiState = uiState.copy(isTagSelectorVisible = it) },
                     onDone = {
                         // call viewmodel to save
                         onDismiss()
                     },
                     onCancel = { onDismiss() }
                 )
             }
        },
        onTaskClick = { task ->
            popupController.push { onDismiss ->
                TaskDetailPopUp(
                    task = task,
                    onEditClick = { taskToEdit ->
                        popupController.push { onDismissEdit ->
                             var editUiState by remember {
                                 mutableStateOf(TaskDetailActionUiState(
                                     title = taskToEdit.title,
                                     taskType = taskToEdit.type,
                                      selectedTags = taskToEdit.tags,
                                     priority = taskToEdit.priority,
                                     description = taskToEdit.description,
                                     selectedStartDateMillis = null,
                                     selectedDueDateMillis = null,
                                     isTagSelectorVisible = false,
                                     selectedProjectId = null,
                                     selectedAssigneeId = null
                                 ))
                             }

                             TaskDetailEditPopUp(
                                 task = taskToEdit,
                                 uiState = editUiState,
                                 availableTags = availableTags,
                                 projectsInWorkspace = projects,
                                 membersInWorkspace = members,
                                 onTitleChange = { editUiState = editUiState.copy(title = it) },
                                 onDescriptionChange = { editUiState = editUiState.copy(description = it) },
                                 onPriorityChange = { editUiState = editUiState.copy(priority = it) },
                                 onToggleTag = { tag ->
                                     val newTags = if (editUiState.selectedTags.contains(tag)) {
                                         editUiState.selectedTags.filter { t -> t.uuid != tag.uuid }
                                     } else {
                                         editUiState.selectedTags + tag
                                     }
                                     editUiState = editUiState.copy(selectedTags = newTags)
                                 },
                                 onProjectSelected = { editUiState = editUiState.copy(selectedProjectId = it) },
                                 onAssigneeSelected = { editUiState = editUiState.copy(selectedAssigneeId = it) },
                                 onTagSelectorVisibilityChange = { editUiState = editUiState.copy(isTagSelectorVisible = it) },
                                 onDone = {
                                     // call viewmodel to updated
                                     onDismissEdit()
                                 },
                                 onCancel = { onDismissEdit() }
                             )
                         }
                    },
                    onClose = { onDismiss() },
                    onDone = { onDismiss() },
                    onWontDo = { onDismiss() },
                    onDelete = { onDismiss() }
                )
            }
        }
    )
}
