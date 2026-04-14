package com.example.se405.android_native_frontend.features.tasks_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android_native_frontend.features.tasks_management.__test_data__.mock.MockTaskApi
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Project
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Workspace
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.TagUseCases
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.TaskUseCases
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.WorkspaceUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskManagementViewModel(
    private val taskUseCases: TaskUseCases,
    private val tagUseCases: TagUseCases,
    private val workspaceUseCases: WorkspaceUseCases
): ViewModel() {

    private val mockApi = MockTaskApi()

    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    private val _availableTags = MutableStateFlow<List<Tag>>(emptyList())
    val availableTags: StateFlow<List<Tag>> = _availableTags.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _members = MutableStateFlow<List<WorkspaceMember>>(emptyList())
    val members: StateFlow<List<WorkspaceMember>> = _members.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        viewModelScope.launch {
            _workspaces.value = mockApi.getWorkspaces()
            _availableTags.value = mockApi.getAvailableTags()
            _projects.value = mockApi.getProjectsInWorkspace()
            _members.value = mockApi.getMembersInWorkspace()
        }
    }
}