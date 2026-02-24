package com.example.se405.android_native_frontend.di

import org.koin.dsl.module

/**
 * Defines the core application dependencies that should live for the entire app lifecycle.
 *
 * This module typically includes:
 * - Singleton instances (e.g., Network clients, Database providers).
 * - Shared Preferences or Local Storage managers.
 * - Repositories that act as a single source of truth.
 */
val appModule = module {
    single  {  }
}

/**
 * Defines the dependency injection rules for ViewModels.
 * * Each ViewModel is declared using a factory pattern, ensuring that the UI components
 * (Activities/Fragments) receive a fresh instance or the correctly scoped instance
 * according to the ViewModelStoreOwner's lifecycle.
 */
val viewModelModule = module {
}
