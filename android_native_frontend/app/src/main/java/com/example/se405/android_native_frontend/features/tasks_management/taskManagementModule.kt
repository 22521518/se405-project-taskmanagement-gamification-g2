package com.example.se405.android_native_frontend.features.tasks_management

import com.example.se405.android_native_frontend.features.tasks_management.data.remote.*
import com.example.se405.android_native_frontend.features.tasks_management.data.repository.*
import com.example.se405.android_native_frontend.features.tasks_management.domain.repository.*
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.*
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.CreateTag
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.DeleteTag
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetMembersByWorkspace
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetProjectsByWorkspace
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetTagsByUser
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetTagsByWorkspace
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetTagsForTaskOwnership
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.GetTask
import com.example.se405.android_native_frontend.features.tasks_management.domain.use_case.crud.UpdateTag
import com.example.se405.android_native_frontend.features.tasks_management.presentation.viewmodel.TaskManagementViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val taskDomainModule = module {
    factoryOf(::GetTask)
    factoryOf(::CreateTag)
    factoryOf(::DeleteTag)
    factoryOf(::GetTagsByUser)
    factoryOf(::GetTagsByWorkspace)
    factoryOf(::GetTagsForTaskOwnership)
    factoryOf(::UpdateTag)
    factoryOf(::GetProjectsByWorkspace)
    factoryOf(::GetMembersByWorkspace)

    factoryOf(::TagUseCases)
    factoryOf(::WorkspaceUseCases)
}

val taskDataModule = module {
    singleOf(::TaskApiImpl) { bind<TaskApi>() }
    singleOf(::TagApiImpl) { bind<TagApi>() }
    singleOf(::WorkspaceApiImpl) { bind<WorkspaceApi>() }

    singleOf(::TaskRepositoryImpl) { bind<TaskRepository>() }
    singleOf(::TagRepositoryImpl) { bind<TagRepository>() }
    singleOf(::WorkspaceRepositoryImpl) { bind<WorkspaceRepository>() }
}

val taskPresentationModule = module {
    viewModelOf(::TaskManagementViewModel)
}

val taskManagementModule = listOf(taskDomainModule, taskDataModule, taskPresentationModule)