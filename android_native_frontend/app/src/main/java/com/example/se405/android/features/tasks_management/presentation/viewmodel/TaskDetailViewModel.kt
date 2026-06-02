@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.navigations.UuidTypeMap
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceRole
import com.example.se405.android.features.tasks_management.domain.repository.TaskCompletionLogRepository
import com.example.se405.android.features.tasks_management.domain.repository.TaskRepository
import com.example.se405.android.features.tasks_management.presentation.TaskDetailNav
import com.example.se405.android.features.workspaces_management.domain.use_case.ExtWorkspaceWithGetCreateUseCases
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceUiEvent
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class TaskDetailInfo(
    val task: Task,
    val currentUserId: Uuid,
    val canDelete: Boolean,
    val canCompleteOrFail: Boolean
)

typealias TaskDetailUiState = WorkspaceUiState<TaskDetailInfo>

class TaskDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val authPreferences: AuthPreferences,
    private val taskRepository: TaskRepository,
    private val taskCompletionLogRepository: TaskCompletionLogRepository,
    private val workspaceUseCase: ExtWorkspaceWithGetCreateUseCases,
) : ViewModel() {

    private val taskNav = savedStateHandle.toRoute<TaskDetailNav>(UuidTypeMap)
    val taskId = taskNav.taskId

    private val _rawTask = MutableStateFlow<Task?>(null)
    private val _isActionLoading = MutableStateFlow(false)
    private val _taskLoaded = MutableStateFlow(false)

    private val _canDelete = MutableStateFlow(false)
    val canDelete: StateFlow<Boolean> = _canDelete.asStateFlow()

    private val _canCompleteOrFail = MutableStateFlow(false)
    val canCompleteOrFail: StateFlow<Boolean> = _canCompleteOrFail.asStateFlow()

    private val _projectName = MutableStateFlow<String?>(null)
    val projectName: StateFlow<String?> = _projectName.asStateFlow()

    private val _workspaceName = MutableStateFlow<String?>(null)
    val workspaceName: StateFlow<String?> = _workspaceName.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _uiEvent = Channel<WorkspaceUiEvent>(Channel.BUFFERED)
    val uiEvent: Flow<WorkspaceUiEvent> = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<TaskDetailUiState> = combine(
        _rawTask,
        _taskLoaded,
        _isActionLoading,
        _canDelete,
        _canCompleteOrFail
    ) { task, loaded, isActionLoading, canDelete, canCompleteOrFail ->
        when {
            !loaded -> WorkspaceUiState.Loading
            task == null -> WorkspaceUiState.NotFound
            else -> WorkspaceUiState.Success(
                data = TaskDetailInfo(
                    task = task,
                    currentUserId = authPreferences.userId.firstOrNull().toUuidOrNull() ?: Uuid.fromLongs(0L, 0L),
                    canDelete = canDelete,
                    canCompleteOrFail = canCompleteOrFail
                ),
                isActionLoading = isActionLoading
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkspaceUiState.Loading)

    init {
        loadTaskDetail()
    }

    fun loadTaskDetail() {
        viewModelScope.launch {
            _isActionLoading.value = true
            _error.value = null
            try {
                val userId = authPreferences.userId.firstOrNull().toUuidOrNull()
                if (userId == null) {
                    _taskLoaded.value = true
                    _rawTask.value = null
                    return@launch
                }

                // Fetch all tasks for the user and find the specific one by taskId
                val userTasks = taskRepository.getTask(userId)
                val task = userTasks.find { it.uuid == taskId }
                _rawTask.value = task
                _taskLoaded.value = true

                if (task != null) {
                    calculatePermissionsAndContext(task, userId)
                }
            } catch (e: Exception) {
                _taskLoaded.value = true
                _rawTask.value = null
                _error.value = e.localizedMessage ?: "Failed to load task details"
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to load task details"))
                Log.e("TaskDetailViewModel", "Error loading task", e)
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    private suspend fun calculatePermissionsAndContext(task: Task, userId: Uuid) {
        val isCreator = task.creator?.uuid == userId
        
        if (task.type == TaskType.HABIT) {
            _canDelete.value = isCreator
            _canCompleteOrFail.value = true
            _projectName.value = null
            _workspaceName.value = null
        } else {
            val projectId = task.projectId
            if (projectId != null) {
                try {
                    val projectOpt = workspaceUseCase.getProjectByProjectId(projectId)
                    if (projectOpt.isPresent) {
                        val project = projectOpt.get()
                        _projectName.value = project.name
                        
                        val workspaceId = runCatching { Uuid.parse(project.workspaceId) }.getOrNull()
                        if (workspaceId != null) {
                            val workspaceOpt = workspaceUseCase.getWorkspaceByWorkspaceId(workspaceId)
                            if (workspaceOpt.isPresent) {
                                val workspace = workspaceOpt.get()
                                _workspaceName.value = workspace.name
                                
                                val currentUserMember = workspace.members.find { it.userId == userId }
                                val isWorkspaceOwner = currentUserMember?.role?.name == WorkspaceRole.OWNER.name
                                _canDelete.value = isWorkspaceOwner || isCreator
                            } else {
                                _canDelete.value = isCreator
                            }
                        } else {
                            _canDelete.value = isCreator
                        }
                    } else {
                        _canDelete.value = isCreator
                    }
                } catch (e: Exception) {
                    Log.e("TaskDetailViewModel", "Failed to fetch project/workspace context for permissions", e)
                    _canDelete.value = isCreator
                }
            } else {
                _canDelete.value = isCreator
            }

            _canCompleteOrFail.value = if (task.assignees.isNotEmpty()) {
                task.assignees.any { it.uuid == userId }
            } else {
                true
            }
        }
    }

    fun markTaskDone() {
        val task = _rawTask.value ?: return
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val userId = authPreferences.userId.firstOrNull().toUuidOrNull()
                if (userId == null) {
                    _uiEvent.send(WorkspaceUiEvent.ShowToast("User not authenticated"))
                    return@launch
                }
                val log = taskCompletionLogRepository.createCompletionLog(
                    task = task,
                    userId = userId,
                    status = TaskStatus.DONE,
                    date = LocalDate.now()
                )
                if (log.isPresent) {
                    _uiEvent.send(WorkspaceUiEvent.ShowToast("Task marked as Done"))
                    loadTaskDetail() // reload to refresh completed count / status
                } else {
                    _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to mark task as Done"))
                }
            } catch (e: Exception) {
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                Log.e("TaskDetailViewModel", "Error marking task done", e)
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun markTaskWontDo() {
        val task = _rawTask.value ?: return
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val userId = authPreferences.userId.firstOrNull().toUuidOrNull()
                if (userId == null) {
                    _uiEvent.send(WorkspaceUiEvent.ShowToast("User not authenticated"))
                    return@launch
                }
                val log = taskCompletionLogRepository.createCompletionLog(
                    task = task,
                    userId = userId,
                    status = TaskStatus.FAILED,
                    date = LocalDate.now()
                )
                if (log.isPresent) {
                    _uiEvent.send(WorkspaceUiEvent.ShowToast("Task marked as Cancelled"))
                    loadTaskDetail() // reload
                } else {
                    _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to mark task"))
                }
            } catch (e: Exception) {
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                Log.e("TaskDetailViewModel", "Error marking task wont do", e)
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun deleteTask(onSuccess: () -> Unit = {}) {
        val task = _rawTask.value ?: return
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val deleted = taskRepository.deleteTask(task)
                if (deleted.isPresent) {
                    _uiEvent.send(WorkspaceUiEvent.ShowToast("Task deleted successfully"))
                    onSuccess()
                } else {
                    _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to delete task"))
                }
            } catch (e: Exception) {
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                Log.e("TaskDetailViewModel", "Error deleting task", e)
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    private fun String?.toUuidOrNull(): Uuid? {
        if (this.isNullOrBlank()) return null
        return runCatching { Uuid.parse(this) }.getOrNull()
    }
}
