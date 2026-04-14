package com.example.se405.android_native_frontend.features.tasks_management.domain.use_case

import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetMembersByWorkspace
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetProjectsByWorkspace

data class WorkspaceUseCases(
    val getProjectsByWorkspace: GetProjectsByWorkspace,
    val getMembersByWorkspace: GetMembersByWorkspace,
)