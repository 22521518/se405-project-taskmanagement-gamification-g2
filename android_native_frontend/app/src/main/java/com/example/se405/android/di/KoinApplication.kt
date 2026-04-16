package com.example.se405.android.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.example.se405.android.features.tasks_management.taskManagementModule

class KoinApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@KoinApplication)
            modules(appModule, viewModelModule)
            modules(taskManagementModule)
        }
    }
}