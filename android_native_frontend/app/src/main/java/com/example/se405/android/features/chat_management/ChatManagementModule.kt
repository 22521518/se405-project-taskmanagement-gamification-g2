package com.example.se405.android.features.chat_management

import com.example.se405.android.features.chat_management.data.repositoryImpl.ChatRepositoryImpl
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import com.example.se405.android.features.chat_management.presentation.viewmodel.ConversationListViewModel
import com.example.se405.android.features.chat_management.presentation.viewmodel.TaskChatViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val chatPresentationModule = module {
    singleOf(::ChatRepositoryImpl) { bind<ChatRepository>() }

    viewModelOf(::TaskChatViewModel)
    viewModelOf(::ConversationListViewModel)
}

val chatManagementModule = listOf(chatPresentationModule)
