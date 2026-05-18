package com.example.se405.android.features.chat_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.features.users_management.domain.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewMessageViewModel(
    // Tùy vào kiến trúc của bạn, đây có thể là UserRepository hoặc UserUseCases
    private val userRepository: com.example.se405.android.features.users_management.domain.repository.UserRepository
) : ViewModel() {

    // 1. Chứa danh sách người dùng
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // 2. Chứa trạng thái Loading (để hiển thị vòng xoay xoay)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 3. Chứa thông báo lỗi (nếu rớt mạng)
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Ngay khi mở màn hình Tạo tin nhắn, tự động gọi API lấy danh sách User
        loadAllUsers()
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Gọi xuống tầng Data/Repository để lấy dữ liệu
                val userList = userRepository.getAllUsers()
                _users.value = userList
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.message ?: "Không thể tải danh sách người dùng"
            } finally {
                _isLoading.value = false
            }
        }
    }
}