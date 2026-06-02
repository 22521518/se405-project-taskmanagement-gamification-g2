@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management

import com.example.se405.android.features.tasks_management.data.remote.*
import com.example.se405.android.features.tasks_management.data.repository.*
import com.example.se405.android.features.tasks_management.domain.repository.*
import com.example.se405.android.features.tasks_management.domain.use_case.*
import com.example.se405.android.features.tasks_management.domain.use_case.crud.*
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskManagementViewModel
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.uuid.ExperimentalUuidApi

val taskDomainModule = module {
    factory {
        val taskRepo: TaskRepository = get()
        TaskUseCases(
            getTask = taskRepo::getTask,
            updateTask = taskRepo::updateTask,
            deleteTask = taskRepo::deleteTask,
            createTask = taskRepo::createTask,
        )
    }

    factory {
        val personalTaskRepository: PersonalTaskRepository = get()
        PersonalTaskUsecase(personalTaskRepository)
    }

    factory {
        val tagRepo: TagRepository = get()
        TagUseCases(
            getTagsForTaskOwnership = tagRepo::getTagsForTaskOwnership,
            getTagsByWorkspace = tagRepo::getTagsByWorkspace,
            getTagsByUser = tagRepo::getTagsByUser,
            createTag = tagRepo::createTag,
            updateTag = tagRepo::updateTag,
            deleteTag = tagRepo::deleteTag,
        )
    }

    factory {
        val workspaceRepo: WorkspaceRepository = get()
        WorkspaceUseCases(workspaceRepo)
    }

    factory { MarkTaskDone(get()) }
    factory { MarkTaskWontDo(get()) }
}

val taskDataModule = module {
    singleOf(::TaskApiImpl) { bind<TaskApi>() }
    singleOf(::PersonalTaskApiImpl) { bind<PersonalTaskApi>() }
    singleOf(::TagApiImpl) { bind<TagApi>() }
    singleOf(::WorkspaceApiImpl) { bind<WorkspaceApi>() }

    singleOf(::TaskRepositoryImpl) { bind<TaskRepository>() }
    singleOf(::PersonalTaskRepositoryImpl) { bind<PersonalTaskRepository>() }
    singleOf(::TagRepositoryImpl) { bind<TagRepository>() }
    singleOf(::WorkspaceRepositoryImpl) { bind<WorkspaceRepository>() }
    singleOf(::TaskCompletionLogRepositoryImpl) { bind<TaskCompletionLogRepository>() }
}

val taskPresentationModule = module {
    viewModelOf(::TaskManagementViewModel)
    viewModelOf(::TaskDetailViewModel)
}

val taskManagementModule = listOf(taskDomainModule, taskDataModule, taskPresentationModule)