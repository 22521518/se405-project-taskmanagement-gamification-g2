package com.example.se405.android.core.authentication.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

interface AuthPreferences {
    val authToken: Flow<String?>
    val isBiometricEnabled: Flow<Boolean>
    val userId: Flow<String?>
    val username: Flow<String?>
    val displayName: Flow<String?>

    suspend fun clearAuth()

    suspend fun saveAuth(
        token: String,
        userId: String,
        username: String,
        displayName: String,
        biometricEnabled: Boolean
    )

    suspend fun setBiometricEnabled(enabled: Boolean)
}

class AuthPreferencesImpl(
    private val context: Context
) : AuthPreferences {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
        val USERNAME_KEY = stringPreferencesKey("username")
        val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
        val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    override val authToken: Flow<String?> =
        context.dataStore.data.map { it[TOKEN_KEY] }

    override val isBiometricEnabled: Flow<Boolean> =
        context.dataStore.data.map {
            it[BIOMETRIC_ENABLED_KEY] ?: false
        }

    override val userId: Flow<String?> =
        context.dataStore.data.map { it[USER_ID_KEY] }

    override val username: Flow<String?> =
        context.dataStore.data.map { it[USERNAME_KEY] }

    override val displayName: Flow<String?> =
        context.dataStore.data.map { it[DISPLAY_NAME_KEY] }

    override suspend fun saveAuth(
        token: String,
        userId: String,
        username: String,
        displayName: String,
        biometricEnabled: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USERNAME_KEY] = username
            prefs[DISPLAY_NAME_KEY] = displayName
            prefs[BIOMETRIC_ENABLED_KEY] = biometricEnabled
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    override suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USERNAME_KEY)
            prefs.remove(DISPLAY_NAME_KEY)

            prefs[BIOMETRIC_ENABLED_KEY] = false
        }
    }
}