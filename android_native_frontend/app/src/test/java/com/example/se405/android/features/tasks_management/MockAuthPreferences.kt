package com.example.se405.android.features.tasks_management

import com.example.se405.android.core.authentication.data.AuthPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MockAuthPreferences: AuthPreferences {
    private val _tokenFlow = MutableStateFlow<String?>(null)
    private val _isBiometricEnabled = MutableStateFlow<Boolean>(false)
    private val _userId = MutableStateFlow<String?>(null)
    private val _username = MutableStateFlow<String?>(null)
    private val _displayName = MutableStateFlow<String?>(null)


    override val authToken: Flow<String?> = _tokenFlow
    override val isBiometricEnabled: Flow<Boolean> = _isBiometricEnabled
    override val userId: Flow<String?> = _userId
    override val username: Flow<String?> = _username
    override val displayName: Flow<String?> = _displayName

    override suspend fun clearAuth() {
        _tokenFlow.value = null
        _isBiometricEnabled.value = false
        _userId.value = null
        _username.value = null
        _displayName.value = null
    }

    override suspend fun saveAuth(
        token: String,
        userId: String,
        username: String,
        displayName: String,
        biometricEnabled: Boolean
    ) {
        _tokenFlow.value = token
        _isBiometricEnabled.value = biometricEnabled
        _userId.value = userId
        _username.value = username
        _displayName.value = displayName
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
    }
}