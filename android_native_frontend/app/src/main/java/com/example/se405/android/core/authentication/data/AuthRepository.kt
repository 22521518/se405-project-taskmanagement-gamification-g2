package com.example.se405.android.core.authentication.data

import com.example.se405.android.core.authentication.data.UserProfileResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

@Serializable
data class CloudinaryResponse(
    val secure_url: String
)

class AuthRepository(
    private val client: HttpClient,
    private val prefs: AuthPreferences
) {
    private val baseUrl = com.example.se405.android.di.NetworkConfig.AUTH_URL

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

    suspend fun getMe(): Result<UserProfileResponse> = runCatching {
        val token = prefs.authToken.first() ?: throw Exception("Not authenticated")
        val response = client.get("$baseUrl/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        response.ensureSuccess()
        response.body()
    }

    // Xử lý Đăng xuất (Xóa Local DataStore)
    suspend fun logout() {
        prefs.clearAuth()
    }

    suspend fun uploadAvatarToCloudinary(imageBytes: ByteArray): Result<String> = runCatching {
        val cloudName = "de5l5byyn"
        val uploadPreset = "se405_avatar_upload"
        val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

        // Khởi tạo Client với OkHttp và Logging
        val cleanClient = HttpClient(io.ktor.client.engine.okhttp.OkHttp) {
            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        android.util.Log.d("CLOUDINARY_LOG", message)
                    }
                }
            }
        }

        cleanClient.use { client ->
            val response = client.submitFormWithBinaryData(
                url = url,
                formData = formData {
                    // 1. Thêm upload_preset
                    append("upload_preset", uploadPreset)

                    // 2. Thêm file ảnh
                    append("file", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        // ĐÃ SỬA: Chỉ để đúng tên file, Ktor sẽ tự thêm phần form-data; name="file"
                        append(HttpHeaders.ContentDisposition, "filename=\"avatar.jpg\"")
                    })
                }
            )

            response.ensureSuccess()

            val responseBody = response.bodyAsText()

            val jsonParser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val cloudinaryResponse = jsonParser.decodeFromString<CloudinaryResponse>(responseBody)

            cloudinaryResponse.secure_url
        }
    }

    suspend fun updateProfile(displayName: String, email: String, avatarUrl: String?): Result<UserProfileResponse> = runCatching {
        val token = prefs.authToken.first() ?: throw Exception("Not authenticated")
        val response = client.put("$baseUrl/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(mapOf("displayName" to displayName, "email" to email, "avatarUrl" to avatarUrl))
        }
        response.ensureSuccess()
        response.body()
    }
}