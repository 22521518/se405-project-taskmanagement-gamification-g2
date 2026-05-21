package com.example.se405.android.core.authentication.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthPreferences(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
        val USERNAME_KEY = stringPreferencesKey("username")
        val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
        val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }
    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[BIOMETRIC_ENABLED_KEY] ?: false }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }
    val displayName: Flow<String?> = context.dataStore.data.map { it[DISPLAY_NAME_KEY] }

    suspend fun saveAuth(token: String, userId: String, username: String, displayName: String, biometricEnabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USERNAME_KEY] = username
            prefs[DISPLAY_NAME_KEY] = displayName
            prefs[BIOMETRIC_ENABLED_KEY] = biometricEnabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USERNAME_KEY)
            prefs.remove(DISPLAY_NAME_KEY)
            // We usually keep BIOMETRIC_ENABLED_KEY or clear it based on requirement.
            // Let's clear it too for a full logout if desired, 
            // but often biometric status is per-device.
            prefs[BIOMETRIC_ENABLED_KEY] = false
        }
    }
}
