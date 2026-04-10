package com.example.se405.android_native_frontend.features.tasks_management.presentation.viewmodel

import androidx.lifecycle.ViewModel

import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.GetTask
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.TagUseCases
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.WorkspaceUseCases

class TaskManagementViewModel(
    private val getTask: GetTask,
    private val tagUseCases: TagUseCases,
    private val workspaceUseCases: WorkspaceUseCases
): ViewModel() {
}