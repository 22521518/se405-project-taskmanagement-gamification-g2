package com.example.se405.android.features.tasks_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.Workspace
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.tasks_management.domain.use_case.TagUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.TaskUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.WorkspaceUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskManagementViewModel(
    private val taskUseCases: TaskUseCases,
    private val tagUseCases: TagUseCases,
    private val workspaceUseCases: WorkspaceUseCases
): ViewModel() {

    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    private val _availableTags = MutableStateFlow<List<Tag>>(emptyList())
    val availableTags: StateFlow<List<Tag>> = _availableTags.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _members = MutableStateFlow<List<WorkspaceMember>>(emptyList())
    val members: StateFlow<List<WorkspaceMember>> = _members.asStateFlow()

    init {
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
            }
        }
    }
}