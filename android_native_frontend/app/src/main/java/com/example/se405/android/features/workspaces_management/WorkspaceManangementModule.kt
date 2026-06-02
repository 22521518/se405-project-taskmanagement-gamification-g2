package com.example.se405.android.features.workspaces_management

import com.example.se405.android.features.workspaces_management.data.remote.CreateGetWorkspaceByWorkspaceIdApi
import com.example.se405.android.features.workspaces_management.data.remote.GetCreateWorkspaceByWorkspaceIdApiImpl
import com.example.se405.android.features.workspaces_management.data.repository.ExtWorkspaceRepositoryWithGetImpl
import com.example.se405.android.features.workspaces_management.domain.repository.ExtWorkspaceRepositoryWithGet
import com.example.se405.android.features.workspaces_management.domain.use_case.ExtWorkspaceWithGetCreateUseCases
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.ProjectDetailViewModel
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceManagementViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.uuid.ExperimentalUuidApi

val workspaceDataModule = module {
    singleOf(::GetCreateWorkspaceByWorkspaceIdApiImpl) { bind<CreateGetWorkspaceByWorkspaceIdApi>() }
    singleOf(::ExtWorkspaceRepositoryWithGetImpl) { bind<ExtWorkspaceRepositoryWithGet>() }
}

val workspaceDomainModule = module {
    factoryOf(::ExtWorkspaceWithGetCreateUseCases)
}


@OptIn(ExperimentalUuidApi::class)
val wsViewModelModule = module {
    viewModelOf(::WorkspaceManagementViewModel)
    viewModelOf(::ProjectDetailViewModel)
}

val workspaceManagementModule = listOf(workspaceDataModule, workspaceDomainModule, wsViewModelModule)