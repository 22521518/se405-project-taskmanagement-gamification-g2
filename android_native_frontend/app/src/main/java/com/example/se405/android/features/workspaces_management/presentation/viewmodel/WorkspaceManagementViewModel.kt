@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import com.example.se405.android.features.workspaces_management.domain.use_case.ExtWorkspaceWithGetCreateUseCases
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface WorkspaceUiEvent {
    data class ShowToast(val message: String) : WorkspaceUiEvent
}

sealed interface WorkspaceUiState<out T> {
    data object Loading : WorkspaceUiState<Nothing>
    data class Success<T>(val data: T, val isActionLoading: Boolean = false) : WorkspaceUiState<T>
    data object NotFound : WorkspaceUiState<Nothing>
}
typealias WorkspaceListUiState = WorkspaceUiState<List<Workspace>>

class WorkspaceManagementViewModel(
    private val authPreferences: AuthPreferences,
    private val workspaceUseCase: ExtWorkspaceWithGetCreateUseCases,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _rawWorkspaces = MutableStateFlow<List<Workspace>?>(null)

    // UI Notification for event
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _uiEvent = kotlinx.coroutines.channels.Channel< WorkspaceUiEvent>(kotlinx.coroutines.channels.Channel.BUFFERED)
    private val _isActionLoading = MutableStateFlow(false)
    val uiEvent: Flow<WorkspaceUiEvent> = _uiEvent.receiveAsFlow()
    val uiState: StateFlow<WorkspaceListUiState> = combine(_rawWorkspaces, _searchQuery, _isActionLoading) { workspaces, query, isActionLoading ->
        val state: WorkspaceListUiState = when {
            workspaces == null -> WorkspaceUiState.Loading
            query.isEmpty() -> WorkspaceUiState.Success(workspaces, isActionLoading)
            else -> WorkspaceUiState.Success(workspaces.filter { it.name.contains(query, ignoreCase = true) }, isActionLoading)
        }
        state
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkspaceUiState.Loading)

    init {
        viewModelScope.launch { loadWorkspace() }
    }

    private suspend fun loadWorkspace() {
        _isActionLoading.value = true
        try {
            val userId = authPreferences.userId.firstOrNull().toUuidOrNull()
            if (userId == null) {
                clearSessionState()
                return
            }
            val workspaceList = workspaceUseCase.getWorkspaceListByUserId(userId)
            _rawWorkspaces.value = workspaceList
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Failed to load workspaces"
            _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to load workspaces"))
            Log.e("WSMAN_LOAD_WS", e.toString())
        } finally {
            _isActionLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun clearSessionState() {
        _rawWorkspaces.value = emptyList()
    }

    fun createWorkspace(workSpaceName: String) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val userId = authPreferences.userId.firstOrNull().toUuidOrNull()
                if (userId == null) {
                    clearSessionState()
                    return@launch
                }
                workspaceUseCase.createWorkspace(userId = userId, workspaceName = workSpaceName)
                loadWorkspace()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to create workspace"
                _uiEvent.send(WorkspaceUiEvent.ShowToast("Failed to create workspace"))
                Log.e("WSMAN_CREATE_WS", e.toString())
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