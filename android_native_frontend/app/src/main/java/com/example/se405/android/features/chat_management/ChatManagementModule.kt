package com.example.se405.android.features.chat_management

import com.example.se405.android.features.chat_management.presentation.viewmodel.TaskChatViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val chatPresentationModule = module {
    viewModelOf(::TaskChatViewModel)
}

val chatManagementModule = listOf(chatPresentationModule)