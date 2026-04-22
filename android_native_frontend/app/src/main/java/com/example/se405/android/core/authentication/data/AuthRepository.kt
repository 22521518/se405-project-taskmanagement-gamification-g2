package com.example.se405.android.core.authentication.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val client: HttpClient,
    private val prefs: AuthPreferences
) {
    private val baseUrl = "http://192.168.1.47:8080/api/auth" // Adjust based on environment

    suspend fun login(req: LoginRequest): Result<AuthResponse> = runCatching {
        val response = client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        response.ensureSuccess()
        response.body()
    }

    suspend fun register(req: RegisterRequest): Result<AuthResponse> = runCatching {
        val response = client.post("$baseUrl/register") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        response.ensureSuccess()
        response.body()
    }

    suspend fun enableBiometric(req: BiometricEnableRequest): Result<SimpleMessageResponse> = runCatching {
        val token = prefs.authToken.first() ?: throw Exception("Not authenticated")
        val response = client.post("$baseUrl/enable-biometric") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        response.ensureSuccess()
        response.body()
    }

    suspend fun disableBiometric(deviceId: String): Result<SimpleMessageResponse> = runCatching {
        val token = prefs.authToken.first() ?: throw Exception("Not authenticated")
        val response = client.post("$baseUrl/disable-biometric") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("deviceId", deviceId)
        }
        response.ensureSuccess()
        response.body()
    }

    suspend fun loginBiometric(req: BiometricLoginRequest): Result<AuthResponse> = runCatching {
        val response = client.post("$baseUrl/login-biometric") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        response.ensureSuccess()
        response.body()
    }

    /**
     * Check response status and throw a descriptive exception for non-2xx responses.
     * This prevents Ktor from trying to deserialize error HTML/text bodies as JSON.
     */
    private suspend fun HttpResponse.ensureSuccess() {
        if (status.value >= 300) {
            val errorBody = try {
                bodyAsText()
            } catch (_: Exception) {
                "No response body"
            }
            throw Exception("HTTP ${status.value}: $errorBody")
        }
    }
}
