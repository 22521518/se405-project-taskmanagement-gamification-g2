@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.navigations.UuidTypeMap
import com.example.se405.android.core.presentation.components.HabitLabel
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.features.workspaces_management.domain.use_case.ExtWorkspaceWithGetCreateUseCases
import com.example.se405.android.features.workspaces_management.presentation.ProjectNav
import com.example.se405.android.features.workspaces_management.presentation.components.AddableMember
import com.example.se405.android.graphql.GetProjectQuery
import kotlinx.coroutines.CoroutineExceptionHandler
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
import kotlin.jvm.optionals.getOrNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias ProjectDetailUiState = WorkspaceUiState<GetProjectQuery.GetProject?>
class ProjectDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val authPreferences: AuthPreferences,
    private val workspaceUseCase: ExtWorkspaceWithGetCreateUseCases,
): ViewModel() {
    private val projectNav = savedStateHandle.toRoute<ProjectNav>(UuidTypeMap)
    val projectId = projectNav.projectId
    private val _rawProjectDetail = MutableStateFlow<GetProjectQuery.GetProject?>(null)
    private val _availableTags = MutableStateFlow<List<Tag>>(emptyList())
    val availableTags: StateFlow<List<Tag>> = _availableTags.asStateFlow()

    // UI Notification for event
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _uiEvent = Channel<WorkspaceUiEvent>(Channel.BUFFERED)
    private val _isActionLoading = MutableStateFlow(false)
    val uiEvent: Flow<WorkspaceUiEvent> = _uiEvent.receiveAsFlow()
    val uiState: StateFlow<ProjectDetailUiState> = combine(_rawProjectDetail, _isActionLoading) { project, isLoading ->
        if (project == null && isLoading) {
            WorkspaceUiState.Loading
        } else {
            WorkspaceUiState.Success(project, isLoading)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkspaceUiState.Loading)

    // ── State cho Add Members popup ───────────────────────────────────────────────

    /**
     * Danh sách workspace members chưa tham gia project này.
     * Được load khi người dùng mở tab MEMBERS và nhấn FAB.
     */
    private val _addableMembersForProject = MutableStateFlow<List<AddableMember>>(emptyList())
    val addableMembersForProject: StateFlow<List<AddableMember>> = _addableMembersForProject.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        _isActionLoading.value = false
        val errMsg = exception.localizedMessage ?: "An unexpected error occurred"
        _error.value = errMsg
        viewModelScope.launch {
            _uiEvent.send(WorkspaceUiEvent.ShowToast(errMsg))
        }
        Log.e("ProjectDetailVM", "Error in coroutine scope", exception)
    }

    init {
        viewModelScope.launch { loadData() }
    }

    private fun loadData() {
        viewModelScope.launch {
            _isActionLoading.value = true
            _error.value = null
            try {
                val userId = authPreferences.userId.firstOrNull().toUuidOrNull()
                if (userId == null) {
                    clearSessionState()
                    _isActionLoading.value = false
                    return@launch
                }
                val project = workspaceUseCase.getProjectByProjectId(projectId).getOrNull()
                _rawProjectDetail.value = project

                project?.workspaceId?.let { wsIdStr ->
                    runCatching { Uuid.parse(wsIdStr) }.getOrNull()?.let { wsId ->
                        _availableTags.value = workspaceUseCase.getTagsByWorkspace(wsId)
                    }
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load project"
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to load project"))
                Log.e("WSMAN_LOAD_WS", e.toString())
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun createTask(
        title: String,
        description: String,
        priority: TaskPriority,
        startDate: LocalDate?,
        dueDate: LocalDate?,
        tagIds: List<Uuid>,
        assigneeId: Uuid?,
    ) {
        viewModelScope.launch(exceptionHandler) {
            if (title.isBlank()) {
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Title cannot be empty"))
                return@launch
            }
            _isActionLoading.value = true
            try {
                val assigneeUser = assigneeId?.let { id ->
                    _rawProjectDetail.value?.members?.find { it.user?.uuid == id.toString() }?.user?.let { user ->
                        User(
                            uuid = Uuid.parse(user.uuid),
                            email = user.email,
                            username = user.username,
                            displayName = user.displayName,
                            avatarUrl = user.avatarUrl,
                            createdAt = null,
                            updatedAt = null
                        )
                    } ?: User(
                        uuid = id,
                        email = "temp@temp.com",
                        username = "temp",
                        displayName = "Temp",
                        avatarUrl = null,
                        createdAt = null,
                        updatedAt = null
                    )
                }
                val assignees = listOfNotNull(assigneeUser)

                workspaceUseCase.createTask(
                    Task(
                        uuid = Uuid.random(),
                        title = title,
                        description = description,
                        repetition = 1,
                        type = TaskType.PROJECT,
                        status = TaskStatus.TODO,
                        priority = priority,
                        projectId = projectId,
                        startDate = startDate,
                        dueDate = dueDate,
                        tagIds = tagIds,
                        assignees = assignees
                    )
                )
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Task created!"))
                loadData() // refresh
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun createTag(
        name: String,
        color: Int,
        label: HabitLabel,
        ownershipType: TagOwnershipType,
    ) {
        viewModelScope.launch(exceptionHandler) {
            val project = _rawProjectDetail.value ?: return@launch
            val wsId = runCatching { Uuid.parse(project.workspaceId) }.getOrNull() ?: return@launch
            workspaceUseCase.createTag(
                Tag(
                    uuid = Uuid.random(), // server sẽ override
                    name = name,
                    color = color,
                    ownershipType = ownershipType,
                    workspaceId = wsId,
                    label = label,
                )
            )
            // Reload tags
            _availableTags.value = workspaceUseCase.getTagsByWorkspace(wsId)
            _isActionLoading.value = false

        }
    }

    fun loadAddableMembersForProject() {
        viewModelScope.launch(exceptionHandler) {
            _isActionLoading.value = true
            try {
                val project = _rawProjectDetail.value ?: return@launch
                val wsId = runCatching { Uuid.parse(project.workspaceId) }.getOrNull() ?: return@launch

                val allWorkspaceMembers = workspaceUseCase.getMembersByWorkspace(wsId)
                val projectMembers = workspaceUseCase.getMemberByProject(projectId)
                val projectMemberIds = projectMembers.map { it.userId }.toSet()

                _addableMembersForProject.value = allWorkspaceMembers
                    .filter { it.userId !in projectMemberIds }
                    .map { wsMember ->
                        AddableMember(
                            userId = wsMember.userId,
                            displayName = wsMember.user?.displayName ?: wsMember.userId.toString(),
                            username = wsMember.user?.username ?: "",
                            avatarUrl = wsMember.user?.avatarUrl,
                        )
                    }
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    /**
     * Thêm nhiều members vào project cùng lúc.
     * Sau khi thành công: reload data + clear danh sách candidates.
     *
     * @param userIds Danh sách userId đã được chọn trong popup
     */
    fun addMembersToProject(userIds: List<Uuid>) {
        if (userIds.isEmpty()) return
        viewModelScope.launch(exceptionHandler) {
            _isActionLoading.value = true
            try {
                workspaceUseCase.addMembersToProject(
                    projectId = projectId,
                    userIds = userIds,
                )
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Added ${userIds.size} member(s) to project!"))
                _addableMembersForProject.value = emptyList() // clear để popup tự đóng
                loadData() // refresh project detail
            } catch (e: Exception) {
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to add members"))
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    private fun clearSessionState() {
        _rawProjectDetail.value = null
    }

    private fun String?.toUuidOrNull(): Uuid? {
        if (this.isNullOrBlank()) return null
        return runCatching { Uuid.parse(this) }.getOrNull()
    }
}
