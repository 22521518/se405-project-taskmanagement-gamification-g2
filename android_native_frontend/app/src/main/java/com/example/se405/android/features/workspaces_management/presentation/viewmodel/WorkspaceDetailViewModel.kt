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
import com.example.se405.android.features.tasks_management.data.remote.toUuidOrNull
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.workspaces_management.data.remote.WorkspaceActivityPage
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import com.example.se405.android.features.workspaces_management.domain.use_case.ExtWorkspaceWithGetCreateUseCases
import com.example.se405.android.features.workspaces_management.presentation.WorkspaceDetailNav
import com.example.se405.android.features.workspaces_management.presentation.components.AddableMember
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

data class WorkspaceDetailInfo(
    val workspace: Workspace,
    val recentActivities: List<TaskCompletionLog>
)

typealias WorkspaceDetailUiState = WorkspaceUiState<WorkspaceDetailInfo>

class WorkspaceDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val authPreferences: AuthPreferences,
    private val workspaceUseCase: ExtWorkspaceWithGetCreateUseCases,
) : ViewModel() {

    private val workspaceNav = savedStateHandle.toRoute<WorkspaceDetailNav>(UuidTypeMap)
    val workspaceId = workspaceNav.workspaceId
    private val _rawWorkspace = MutableStateFlow<Workspace?>(null)
    private val _recentActivityInfo = MutableStateFlow<WorkspaceActivityPage?>(null)
    private val _availableTags = MutableStateFlow<List<Tag>>(emptyList())
    val availableTags: StateFlow<List<Tag>> = _availableTags.asStateFlow()
    private val _workspaceLoaded = MutableStateFlow(false)

    // UI Notification for event
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _uiEvent = kotlinx.coroutines.channels.Channel<WorkspaceUiEvent>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val uiEvent: Flow<WorkspaceUiEvent> = _uiEvent.receiveAsFlow()
    private val _isActionLoading = MutableStateFlow(false)
    val uiState: StateFlow<WorkspaceDetailUiState> = combine(
        _rawWorkspace,
        _recentActivityInfo,
        _workspaceLoaded,
        _isActionLoading,
    ) { workspace, activities, loaded, isActionLoading ->
        when {
            !loaded -> WorkspaceUiState.Loading
            workspace == null -> WorkspaceUiState.NotFound
            else -> WorkspaceUiState.Success(
                data = WorkspaceDetailInfo(
                    workspace = workspace,
                    recentActivities = activities?.content ?: emptyList(),
                ),
                isActionLoading = isActionLoading,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkspaceUiState.Loading)
    // ── State cho Add Members popup ───────────────────────────────────────────────

    /**
     * Danh sách users chưa là workspace member.
     * Map từ GetUsersExcluding → AddableMember.
     */
    private val _addableMembersForWorkspace = MutableStateFlow<List<AddableMember>>(emptyList())
    val addableMembersForWorkspace: StateFlow<List<AddableMember>> = _addableMembersForWorkspace.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch { loadWorkspaceDetail() }
    }

    /**
     * User-initiated pull-to-refresh: re-fetch the workspace from the API. Drives the
     * pull indicator so a manual retry recovers from transient network errors.
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                loadWorkspaceDetail()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun loadWorkspaceDetail() {
        _isActionLoading.value = true
        try {
            val workspaceOptional = workspaceUseCase.getWorkspaceByWorkspaceId(workspaceId)
            _availableTags.value = workspaceUseCase.getTagsByWorkspace(workspaceId)
            _rawWorkspace.value = if (workspaceOptional.isPresent) workspaceOptional.get() else null
            _workspaceLoaded.value = true

            if (_rawWorkspace.value != null) {
                loadRecentActivities()
            }
        } catch (e: Exception) {
            clearSessionState() // reset state về NotFound thay vì treo ở Loading mãi
            _error.value = e.localizedMessage ?: "Failed to load workspace"
            _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to load workspace"))
            Log.e("WS_DETAIL_LOAD", e.toString())
        } finally {
            _isActionLoading.value = false
        }
    }

    private suspend fun loadRecentActivities() {
        try {
            val activities = workspaceUseCase.getWorkspaceActivity(workspaceId)
            _recentActivityInfo.value = activities
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Failed to load recent activities"
            _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to load recent activities"))
            Log.e("WS_DETAIL_ACTIVITY", e.toString())
        }
    }

    fun createProject(projectName: String) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val userId = authPreferences.userId.firstOrNull().toUuidOrNull()
                if (userId == null) {
                    clearSessionState()
                    return@launch
                }
                workspaceUseCase.createProject(
                    userId = userId,
                    workspaceId = workspaceId,
                    projectName = projectName,
                )
                loadWorkspaceDetail()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to create project"
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to create project"))
                Log.e("WS_DETAIL_CREATE_PROJ", e.toString())
            } finally {
                _isActionLoading.value = false
            }
        }
    }


    fun createTask(
        projectId: Uuid,
        title: String,
        description: String,
        priority: TaskPriority,
        startDate: LocalDate?,
        dueDate: LocalDate?,
        tagIds: List<Uuid>,
        assigneeId: Uuid?,
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val assigneeUser = assigneeId?.let { id ->
                    _rawWorkspace.value?.members?.find { it.userId == id }?.user?.let { user ->
                        User(
                            uuid = user.uuid,
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
                loadWorkspaceDetail() // refresh
            } catch (e: Exception) {
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to create task"))
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
        viewModelScope.launch {
            val wsId = workspaceId
            val userId = authPreferences.userId.firstOrNull().toUuidOrNull()
            workspaceUseCase.createTag(
                Tag(
                    uuid = Uuid.random(),
                    createdBy = userId,
                    name = name,
                    color = color,
                    ownershipType = ownershipType,
                    // Keep the ownership ↔ workspaceId invariant the Tag entity enforces:
                    // only WORKSPACE tags may carry a workspaceId.
                    workspaceId = if (ownershipType == TagOwnershipType.WORKSPACE) wsId else null,
                    label = label,
                )
            )
            // Reload tags
            _availableTags.value = workspaceUseCase.getTagsByWorkspace(wsId)
        }
    }

    /**
     * Load danh sách users chưa là workspace member.
     * Gọi khi mở popup "Add Members" ở tab MEMBERS của WorkspaceDetailScreen.
     *
     * Logic:
     *   - Lấy userId từ authPreferences
     *   - Gọi getUsersExcluding(currentUserId) → toàn bộ users ngoài current user
     *   - Lọc tiếp những ai đã là workspace member
     *   - Map sang AddableMember
     */
    fun loadAddableMembersForWorkspace() {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val userId = authPreferences.userId.firstOrNull().toUuidOrNull()
                    ?: return@launch

                val allUsers = workspaceUseCase.getUsersExcluding(userId)
                val currentMemberIds = (_rawWorkspace.value?.members
                    ?.map { it.userId.toString() }
                    ?: emptyList()).toSet()

                _addableMembersForWorkspace.value = allUsers
                    .filter { user -> user.uuid !in currentMemberIds }
                    .map { user ->
                        AddableMember(
                            userId = Uuid.parse(user.uuid),
                            displayName = user.displayName,
                            username = user.username ,
                            avatarUrl = user.avatarUrl,
                        )
                    }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load users"
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to load available members"))
                Log.e("WS_DETAIL_LOAD_MEMBERS", e.toString())
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    /**
     * Thêm nhiều users vào workspace cùng lúc.
     * Sau khi thành công: reload workspace detail + clear danh sách candidates.
     *
     * @param userIds Danh sách userId đã được chọn trong popup
     */
    fun addMembersToWorkspace(userIds: List<Uuid>) {
        if (userIds.isEmpty()) return
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                workspaceUseCase.addMembersToWorkspace(
                    workspaceId = workspaceId,
                    userIds = userIds,
                )
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Added ${userIds.size} member(s) to workspace!"))
                _addableMembersForWorkspace.value = emptyList()
                loadWorkspaceDetail() // refresh
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to add members"
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to add members"))
                Log.e("WS_DETAIL_ADD_MEMBERS", e.toString())
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    private fun clearSessionState() {
        _rawWorkspace.value = null
        _recentActivityInfo.value = null
        _workspaceLoaded.value = true
        _availableTags.value = emptyList()
    }

    fun refreshActivities() {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                loadRecentActivities()
            } finally {
                _isActionLoading.value = false
            }
        }
    }
}
