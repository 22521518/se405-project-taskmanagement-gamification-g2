package com.example.se405.android.features.users_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.core.authentication.data.AuthRepository
import com.example.se405.android.core.authentication.data.UserProfileResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonalViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfileResponse?>(null)
    val userProfile: StateFlow<UserProfileResponse?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            // Gọi repository để lấy data từ endpoint /me
            authRepository.getMe().onSuccess { profile ->
                _userProfile.value = profile
            }.onFailure {e ->
                e.printStackTrace()
                _error.value = "Lỗi tải dữ liệu: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout() // Xóa token
            onSuccess() // Bắn callback để UI chuyển trang
        }
    }
}