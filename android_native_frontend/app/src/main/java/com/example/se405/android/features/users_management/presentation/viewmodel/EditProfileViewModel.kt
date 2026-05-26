package com.example.se405.android.features.users_management.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.core.authentication.data.AuthRepository
import com.example.se405.android.core.utils.ImageUtils // Đảm bảo bạn đã tạo file này ở bước trước
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var displayName by mutableStateOf("")
    var email by mutableStateOf("")

    var avatarUrl by mutableStateOf<String?>(null)

    var selectedImageUri by mutableStateOf<Uri?>(null)

    // Trạng thái UI
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    // Khởi tạo dữ liệu khi mở màn hình (Gọi từ LaunchedEffect)
    fun initData(initialName: String, initialEmail: String, initialAvatarUrl: String? = null) {
        displayName = initialName
        email = initialEmail
        avatarUrl = initialAvatarUrl
    }

    // Hàm thực thi khi bấm nút Save
    fun update(context: Context, onBack: () -> Unit) {
        // 1. Validate dữ liệu cơ bản
        if (displayName.isBlank() || email.isBlank()) {
            error = "Vui lòng nhập đầy đủ Tên và Email!"
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null

            var finalAvatarUrl = avatarUrl

            if (selectedImageUri != null) {
                val imageBytes = ImageUtils.compressUriToByteArray(context, selectedImageUri!!)

                if (imageBytes != null) {
                    val uploadResult = authRepository.uploadAvatarToCloudinary(imageBytes)

                    if (uploadResult.isSuccess) {
                        finalAvatarUrl = uploadResult.getOrNull()
                    } else {
                        val realError = uploadResult.exceptionOrNull()?.message
                        error = "Lỗi Cloudinary: $realError"
                        uploadResult.exceptionOrNull()?.printStackTrace()

                        isLoading = false
                        return@launch
                    }
                } else {
                    error = "Không thể xử lý định dạng ảnh này!"
                    isLoading = false
                    return@launch
                }
            }

            authRepository.updateProfile(
                displayName = displayName,
                email = email,
                avatarUrl = finalAvatarUrl
            ).onSuccess {
                onBack()
            }.onFailure { e ->
                error = "Lỗi máy chủ: ${e.message}"
            }

            isLoading = false
        }
    }
}