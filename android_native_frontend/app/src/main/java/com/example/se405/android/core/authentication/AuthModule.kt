package com.example.se405.android.core.authentication

import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.data.AuthPreferencesImpl
import com.example.se405.android.core.authentication.data.AuthRepository
import com.example.se405.android.core.authentication.managers.AccountBiometricManager
import com.example.se405.android.core.authentication.managers.CryptoManager
import com.example.se405.android.core.authentication.managers.DeviceAuthManager
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule = module {
    single<AuthPreferences> { AuthPreferencesImpl(get()) }
    singleOf(::AuthRepository)
    single { CryptoManager() }
    singleOf(::DeviceAuthManager)
    singleOf(::AccountBiometricManager)
}

val viewModelModule = module {
    viewModelOf(::BiometricViewmodel)
}

val authModule = listOf(dataModule, viewModelModule)
