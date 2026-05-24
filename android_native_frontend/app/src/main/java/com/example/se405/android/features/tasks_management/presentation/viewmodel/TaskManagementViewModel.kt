@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.viewmodel

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
<<<<<<< HEAD
=======
import com.example.se405.android.core.authentication.data.AuthPreferences
>>>>>>> origin/dev
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.entity.Workspace
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.tasks_management.domain.entity.getTaskStatus
import com.example.se405.android.features.tasks_management.domain.use_case.TagUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.TaskUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.WorkspaceUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.crud.MarkTaskDone
import com.example.se405.android.features.tasks_management.domain.use_case.crud.MarkTaskWontDo
import com.example.se405.android.features.tasks_management.presentation.components.CreateTagUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface TaskUiEvent {
    data class ShowToast(val message: String) : TaskUiEvent
    object TaskCreated : TaskUiEvent
    object TaskUpdated : TaskUiEvent
    object TaskDeleted : TaskUiEvent
    object TagCreated : TaskUiEvent
    object TagUpdated : TaskUiEvent
    object TagDeleted : TaskUiEvent
}

class TaskManagementViewModel(
    private val taskUseCases: TaskUseCases,
    private val tagUseCases: TagUseCases,
    private val workspaceUseCases: WorkspaceUseCases,
    private val authPreferences: AuthPreferences,
    private val markTaskDoneUseCase: MarkTaskDone,
    private val markTaskWontDoUseCase: MarkTaskWontDo,
) : ViewModel() {

    private val _allWorkspaces = MutableStateFlow<List<Workspace>>(emptyList())

    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    private val _availableTags = MutableStateFlow<List<Tag>>(emptyList())
    val availableTags: StateFlow<List<Tag>> = _availableTags.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _members = MutableStateFlow<List<WorkspaceMember>>(emptyList())
    val members: StateFlow<List<WorkspaceMember>> = _members.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _isDateFilterActive = MutableStateFlow(true)

    private val _currentUserId = MutableStateFlow<Uuid?>(null)
    val currentUserId: StateFlow<Uuid?> = _currentUserId.asStateFlow()

    private val _currentWorkspaceId = MutableStateFlow<Uuid?>(null)
    val currentWorkspaceId: StateFlow<Uuid?> = _currentWorkspaceId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _uiEvent = kotlinx.coroutines.channels.Channel<TaskUiEvent>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val uiEvent: Flow<TaskUiEvent> = _uiEvent.receiveAsFlow()

    init {
<<<<<<< HEAD
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {

                // Ví dụ (Hãy sửa tên hàm cho đúng với UseCase thực tế của bạn):

                // val fetchedWorkspaces = workspaceUseCases.getWorkspaces()
                // _workspaces.value = fetchedWorkspaces

                // val fetchedTags = tagUseCases.getAllTags()
                // _availableTags.value = fetchedTags

                // val fetchedProjects = workspaceUseCases.getProjects()
                // _projects.value = fetchedProjects

                // val fetchedMembers = workspaceUseCases.getMembers()
                // _members.value = fetchedMembers

            } catch (e: Exception) {
                e.printStackTrace()
=======
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userId = authPreferences.userId.firstOrNull().toUuidOrNull()
                if (userId == null) {
                    clearSessionState()
                    return@launch
                }

                _currentUserId.value = userId
                refreshAll(userId)
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load initial data"
                _uiEvent.send(TaskUiEvent.ShowToast("Network error: failed to load tasks"))
            } finally {
                _isLoading.value = false
>>>>>>> origin/dev
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val userId = _currentUserId.value ?: authPreferences.userId.firstOrNull().toUuidOrNull()
            if (userId == null) {
                clearSessionState()
                return@launch
            }
            _currentUserId.value = userId
            _isLoading.value = true
            _error.value = null
            try {
                refreshAll(userId)
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to refresh data"
                _uiEvent.send(TaskUiEvent.ShowToast("Refresh failed: please check connection"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun clearSessionState() {
        _workspaces.value = emptyList()
        _allWorkspaces.value = emptyList()
        _availableTags.value = emptyList()
        _projects.value = emptyList()
        _members.value = emptyList()
        _tasks.value = emptyList()
        _currentWorkspaceId.value = null
        _currentUserId.value = null
        _isDateFilterActive.value = false
    }

    private suspend fun refreshAll(userId: Uuid) {
        val fetchedTasks = taskUseCases.getTask(userId)
        _tasks.value = fetchedTasks

        val tagsByUser = tagUseCases.getTagsByUser(userId)
        val workspaceId = resolveWorkspaceId(tagsByUser)
        _currentWorkspaceId.value = workspaceId

        val tagsByWorkspace = workspaceId?.let { tagUseCases.getTagsByWorkspace(it) }.orEmpty()
        _availableTags.value = mergeTags(tagsByUser, tagsByWorkspace)

        _projects.value = workspaceId?.let { workspaceUseCases.getProjectsByWorkspace(it) }.orEmpty()
        _members.value = workspaceId?.let { workspaceUseCases.getMembersByWorkspace(it) }.orEmpty()

        rebuildWorkspaces()
    }

    private suspend fun refreshSilently() {
        val userId = _currentUserId.value ?: return
        try {
            refreshAll(userId)
        } catch (e: Exception) {
            android.util.Log.w("TaskManagementVM", "Silent refresh failed", e)
        }
    }

    fun getTaskByDate(calendar: Calendar): List<Workspace> {
        _selectedDate.value = calendar.clone() as Calendar
        _isDateFilterActive.value = true
        applyDateFilter(_selectedDate.value)
        refresh()
        return _workspaces.value
    }

    fun createTaskDraft(taskType: TaskType = TaskType.HABIT): Task {
        return Task(
            uuid = Uuid.random(),
            title = "",
            description = "",
            repetition = 0,
            type = taskType,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            creator = null,
            tags = emptyList(),
            taskCompletionLog = emptyList(),
            startDate = null,
            dueDate = null,
            projectId = null,
        )
    }

    fun createTask(task: Task, onCompleted: (Boolean) -> Unit = {}) {
        android.util.Log.d("TaskManagementVM", "createTask init: task title='${task.title}'")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentId = _currentUserId.value
                val created = taskUseCases.createTask(task, currentId)
                if (created.isPresent) {
                    refreshSilently()
                    val createdTask = created.get()
                    android.util.Log.i("TaskManagementVM", "createTask succeeded: uuid=${createdTask.uuid}")

                    _uiEvent.send(TaskUiEvent.TaskCreated)
                    _uiEvent.send(TaskUiEvent.ShowToast("Task \"${task.title}\" created successfully"))
                    onCompleted(true)
                } else {
                    android.util.Log.e("TaskManagementVM", "createTask failed: empty Optional returned")
                    _uiEvent.send(TaskUiEvent.ShowToast("Failed to create task"))
                    onCompleted(false)
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskManagementVM", "createTask error", e)
                _uiEvent.send(TaskUiEvent.ShowToast("Error: ${e.localizedMessage ?: "Failed to create task"}"))
                onCompleted(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTask(task: Task, onCompleted: (Boolean) -> Unit = {}) {
        android.util.Log.d("TaskManagementVM", "updateTask init: task uuid=${task.uuid}, title='${task.title}'")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updated = taskUseCases.updateTask(task)
                if (updated.isPresent) {

                    refreshSilently()
                    val updatedTask = updated.get()
                    android.util.Log.i("TaskManagementVM", "updateTask succeeded: uuid=${updatedTask.uuid}")
//                    replaceTask(updatedTask)
                    _uiEvent.send(TaskUiEvent.TaskUpdated)
                    _uiEvent.send(TaskUiEvent.ShowToast("Task updated successfully"))
                    onCompleted(true)
                } else {
                    android.util.Log.e("TaskManagementVM", "updateTask failed: empty Optional returned")
                    _uiEvent.send(TaskUiEvent.ShowToast("Failed to update task"))
                    onCompleted(false)
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskManagementVM", "updateTask error", e)
                _uiEvent.send(TaskUiEvent.ShowToast("Error: ${e.localizedMessage ?: "Failed to update task"}"))
                onCompleted(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markTaskDone(task: Task, onCompleted: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = _currentUserId.value
                if (userId == null) {
                    _uiEvent.send(TaskUiEvent.ShowToast("User not authenticated"))
                    onCompleted(false)
                    return@launch
                }
                val targetDate = _selectedDate.value.toLocalDate()
                val log = markTaskDoneUseCase(task, userId, targetDate)
                if (log.isPresent) {
//                    val completedLog = log.get()
//                    val updatedTask = task.copy(taskCompletionLog = task.taskCompletionLog + completedLog)
//                    replaceTask(updatedTask)
                    refreshSilently()
                    _uiEvent.send(TaskUiEvent.ShowToast("Task marked as Done"))
                    onCompleted(true)
                } else {
                    _uiEvent.send(TaskUiEvent.ShowToast("Failed to mark task as done"))
                    onCompleted(false)
                }
            } catch (e: Exception) {
                _uiEvent.send(TaskUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                onCompleted(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markTaskWontDo(task: Task, onCompleted: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = _currentUserId.value
                if (userId == null) {
                    _uiEvent.send(TaskUiEvent.ShowToast("User not authenticated"))
                    onCompleted(false)
                    return@launch
                }
                val targetDate = _selectedDate.value.toLocalDate()
                val log = markTaskWontDoUseCase(task, userId, targetDate)
                if (log.isPresent) {
                    refreshSilently()
//                    val completedLog = log.get()
//                    val updatedTask = task.copy(taskCompletionLog = task.taskCompletionLog + completedLog)
//                    replaceTask(updatedTask)
                    _uiEvent.send(TaskUiEvent.ShowToast("Task marked as Won't Do"))
                    onCompleted(true)
                } else {
                    _uiEvent.send(TaskUiEvent.ShowToast("Failed to mark task"))
                    onCompleted(false)
                }
            } catch (e: Exception) {
                _uiEvent.send(TaskUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                onCompleted(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTask(task: Task, onCompleted: (Boolean) -> Unit = {}) {
        android.util.Log.d("TaskManagementVM", "deleteTask init: task uuid=${task.uuid}")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val deleted = taskUseCases.deleteTask(task)
                if (deleted.isPresent) {
                    val deletedUuid = deleted.get()
                    android.util.Log.i("TaskManagementVM", "deleteTask succeeded: deletedUuid=$deletedUuid")
//                    _tasks.value = _tasks.value.filterNot { it.uuid == deletedUuid }
//                    rebuildWorkspaces()
                    refreshSilently()
                    _uiEvent.send(TaskUiEvent.TaskDeleted)
                    _uiEvent.send(TaskUiEvent.ShowToast("Task deleted successfully"))
                    onCompleted(true)
                } else {
                    android.util.Log.e("TaskManagementVM", "deleteTask failed: empty Optional returned")
                    _uiEvent.send(TaskUiEvent.ShowToast("Failed to delete task"))
                    onCompleted(false)
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskManagementVM", "deleteTask error", e)
                _uiEvent.send(TaskUiEvent.ShowToast("Error: ${e.localizedMessage ?: "Failed to delete task"}"))
                onCompleted(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTag(tagState: CreateTagUiState, onCreated: (Tag?) -> Unit = {}) {
        android.util.Log.d("TaskManagementVM", "createTag init: name='${tagState.name}'")
        val createdBy = _currentUserId.value
        if (createdBy == null || tagState.name.isBlank()) {
            android.util.Log.w("TaskManagementVM", "createTag validation failed: createdBy=$createdBy, name='${tagState.name}'")
            onCreated(null)
            return
        }

        val ownershipType = if (_currentWorkspaceId.value == null) {
            TagOwnershipType.PERSONAL
        } else {
            TagOwnershipType.WORKSPACE
        }

        val tag = Tag(
            createdBy = createdBy,
            ownershipType = ownershipType,
            workspaceId = if (ownershipType == TagOwnershipType.WORKSPACE) _currentWorkspaceId.value else null,
            taskIds = emptyList(),
            uuid = Uuid.random(),
            name = tagState.name.trim(),
            color = tagState.color.toArgb(),
            label = tagState.label,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            creator = null,
            tasks = emptyList(),
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val created = tagUseCases.createTag(tag)
                if (created.isPresent) {
                    refreshSilently()
                    val createdTag = created.get()
                    android.util.Log.i("TaskManagementVM", "createTag succeeded: uuid=${createdTag.uuid}")
//                    _availableTags.value = mergeTags(_availableTags.value, listOf(createdTag))
//                    rebuildWorkspaces()
                    _uiEvent.send(TaskUiEvent.TagCreated)
                    _uiEvent.send(TaskUiEvent.ShowToast("Tag \"${tag.name}\" created successfully"))
                    onCreated(createdTag)
                } else {
                    android.util.Log.e("TaskManagementVM", "createTag failed: empty Optional returned")
                    _uiEvent.send(TaskUiEvent.ShowToast("Failed to create tag"))
                    onCreated(null)
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskManagementVM", "createTag error", e)
                _uiEvent.send(TaskUiEvent.ShowToast("Error: ${e.localizedMessage ?: "Failed to create tag"}"))
                onCreated(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTag(tag: Tag, tagState: CreateTagUiState, onUpdated: (Tag?) -> Unit = {}) {
        android.util.Log.d("TaskManagementVM", "updateTag init: uuid=${tag.uuid}, name='${tagState.name}'")
        if (tagState.name.isBlank()) {
            android.util.Log.w("TaskManagementVM", "updateTag validation failed: name is blank")
            onUpdated(null)
            return
        }

        val updatedTag = tag.copy(
            name = tagState.name.trim(),
            color = tagState.color.toArgb(),
            label = tagState.label,
            updatedAt = LocalDateTime.now(),
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updated = tagUseCases.updateTag(updatedTag)
                if (updated.isPresent) {
                    refreshSilently()
                    val resolved = updated.get()
                    android.util.Log.i("TaskManagementVM", "updateTag succeeded: uuid=${resolved.uuid}")
//                    _availableTags.value = _availableTags.value.map { existing ->
//                        if (existing.uuid == resolved.uuid) resolved else existing
//                    }
//                    _tasks.value = _tasks.value.map { task ->
//                        if (task.tags.any { it.uuid == resolved.uuid }) {
//                            task.copy(tags = task.tags.map { if (it.uuid == resolved.uuid) resolved else it })
//                        } else {
//                            task
//                        }
//                    }
//                    rebuildWorkspaces()
                    _uiEvent.send(TaskUiEvent.TagUpdated)
                    _uiEvent.send(TaskUiEvent.ShowToast("Tag updated successfully"))
                    onUpdated(resolved)
                } else {
                    android.util.Log.e("TaskManagementVM", "updateTag failed: empty Optional returned")
                    _uiEvent.send(TaskUiEvent.ShowToast("Failed to update tag"))
                    onUpdated(null)
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskManagementVM", "updateTag error", e)
                _uiEvent.send(TaskUiEvent.ShowToast("Error: ${e.localizedMessage ?: "Failed to update tag"}"))
                onUpdated(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTag(tagId: Uuid, onCompleted: (Boolean) -> Unit = {}) {
        android.util.Log.d("TaskManagementVM", "deleteTag init: tagId=$tagId")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val deleted = tagUseCases.deleteTag(tagId)
                if (deleted.isPresent) {
                    refreshSilently()
                    android.util.Log.i("TaskManagementVM", "deleteTag succeeded: tagId=$tagId")
//                    _availableTags.value = _availableTags.value.filterNot { it.uuid == tagId }
//                    _tasks.value = _tasks.value.map { task ->
//                        task.copy(tags = task.tags.filterNot { tag -> tag.uuid == tagId })
//                    }
//                    rebuildWorkspaces()
                    _uiEvent.send(TaskUiEvent.TagDeleted)
                    _uiEvent.send(TaskUiEvent.ShowToast("Tag deleted successfully"))
                    onCompleted(true)
                } else {
                    android.util.Log.e("TaskManagementVM", "deleteTag failed: empty Optional returned")
                    _uiEvent.send(TaskUiEvent.ShowToast("Failed to delete tag"))
                    onCompleted(false)
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskManagementVM", "deleteTag error", e)
                _uiEvent.send(TaskUiEvent.ShowToast("Error: ${e.localizedMessage ?: "Failed to delete tag"}"))
                onCompleted(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

//    private fun replaceTask(updatedTask: Task) {
//        _tasks.value = _tasks.value.map { existing ->
//            if (existing.uuid == updatedTask.uuid) updatedTask else existing
//        }
//        rebuildWorkspaces()
//    }

    private fun resolveWorkspaceId(tagsByUser: List<Tag>): Uuid? {
        return tagsByUser.firstOrNull { it.workspaceId != null }?.workspaceId
            ?: _allWorkspaces.value.firstOrNull()?.id
    }

    private fun rebuildWorkspaces(forceRefreshFromRemote: Boolean = false) {
        if (forceRefreshFromRemote) {
            viewModelScope.launch {
                val workspaceId = _currentWorkspaceId.value
                if (workspaceId != null) {
                    _projects.value = workspaceUseCases.getProjectsByWorkspace(workspaceId)
                    _members.value = workspaceUseCases.getMembersByWorkspace(workspaceId)
                }
                buildAndApplyWorkspaces()
            }
        } else {
            buildAndApplyWorkspaces()
        }
    }

    private fun buildAndApplyWorkspaces() {
        val workspaceId = _currentWorkspaceId.value ?: FALLBACK_WORKSPACE_ID
        val workspaceName = _allWorkspaces.value.firstOrNull()?.name ?: "Workspace"
        val mergedProjects = mergeProjectsWithTasks(_projects.value, _tasks.value)
        val workspace = Workspace(
            id = workspaceId,
            name = workspaceName,
            projects = mergedProjects,
            members = _members.value,
        )
        _allWorkspaces.value = listOf(workspace)
        applyDateFilter(_selectedDate.value)
    }

    private fun applyDateFilter(calendar: Calendar?) {
        if (!_isDateFilterActive.value || calendar == null) {
            _workspaces.value = _allWorkspaces.value
            return
        }

        val targetDate = calendar.toLocalDate()
        _workspaces.value = filterWorkspacesByDate(_allWorkspaces.value, targetDate)
    }

    private fun filterWorkspacesByDate(
        workspaces: List<Workspace>,
        targetDate: LocalDate,
        test: Boolean = false
    ): List<Workspace> {
        if (test) return workspaces
        return workspaces.mapNotNull { workspace ->
            val filteredProjects = workspace.projects.mapNotNull { project ->
                val filteredTasks = project.tasks
                    .filter { task -> task.matchesDate(targetDate) }
                    .map { task -> getTaskStatus(task, targetDate) }
                if (filteredTasks.isNotEmpty()) project.copy(tasks = filteredTasks) else null
            }

            if (filteredProjects.isNotEmpty()) workspace.copy(projects = filteredProjects) else null
        }
    }

    private fun mergeTags(primary: List<Tag>, secondary: List<Tag>): List<Tag> {
        val merged = linkedMapOf<Uuid, Tag>()
        (primary + secondary).forEach { tag -> merged[tag.uuid] = tag }
        return merged.values.toList()
    }

    private fun mergeProjectsWithTasks(projects: List<Project>, tasks: List<Task>): List<Project> {
        val tasksByProjectId = tasks.groupBy { it.projectId }
        val knownProjectIds = projects.map { it.id }.toSet()

        val mergedProjects = projects.map { project ->
            project.copy(tasks = tasksByProjectId[project.id].orEmpty())
        }.toMutableList()

        val missingProjectIds = tasksByProjectId.keys
            .filterNotNull()
            .filterNot { it in knownProjectIds }

        missingProjectIds.forEach { missingId ->
            val shortId = missingId.toString().take(8)
            mergedProjects.add(
                Project(
                    id = missingId,
                    name = "Project $shortId",
                    tasks = tasksByProjectId[missingId].orEmpty(),
                )
            )
        }

        val unassignedTasks = tasksByProjectId[null].orEmpty()
        if (unassignedTasks.isNotEmpty() && mergedProjects.none { it.id == UNASSIGNED_PROJECT_ID }) {
            mergedProjects.add(
                Project(
                    id = UNASSIGNED_PROJECT_ID,
                    name = "Personal",
                    tasks = unassignedTasks,
                )
            )
        }

        if (mergedProjects.isEmpty() && tasks.isNotEmpty()) {
            mergedProjects.add(
                Project(
                    id = UNASSIGNED_PROJECT_ID,
                    name = "Tasks",
                    tasks = tasks,
                )
            )
        }

        return mergedProjects
    }

    private fun Calendar.toLocalDate(): LocalDate {
        return LocalDate.of(
            get(Calendar.YEAR),
            get(Calendar.MONTH) + 1,
            get(Calendar.DAY_OF_MONTH),
        )
    }

    private fun Task.matchesDate(targetDate: LocalDate): Boolean {
        if (type == TaskType.HABIT) return true
        val start = startDate ?: dueDate ?: return false
        val end = dueDate ?: startDate ?: return false
        return !targetDate.isBefore(start) && !targetDate.isAfter(end)
    }

    private fun String?.toUuidOrNull(): Uuid? {
        if (this.isNullOrBlank()) return null
        return runCatching { Uuid.parse(this) }.getOrNull()
    }

    private companion object {
        val FALLBACK_WORKSPACE_ID: Uuid = Uuid.fromLongs(0L, 0L)
        val UNASSIGNED_PROJECT_ID: Uuid = Uuid.fromLongs(0L, 1L)
    }
}