package com.example.se405.android.di

import android.app.Application
import com.example.se405.android.core.authentication.authModule
import com.example.se405.android.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.example.se405.android.features.tasks_management.taskManagementModule
import com.example.se405.android.features.chat_management.chatManagementModule

class KoinApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@KoinApplication)
            modules(appModule, networkModule, viewModelModule)
            modules(authModule)
            modules(taskManagementModule)
            modules(chatManagementModule)
        }
    }
}