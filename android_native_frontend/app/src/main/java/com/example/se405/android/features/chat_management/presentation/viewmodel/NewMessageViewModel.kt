package com.example.se405.android.features.chat_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.features.users_management.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewMessageViewModel(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
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
        loadAllUsers()
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
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

    // 4. HÀM MỚI: Xử lý logic tạo phòng chat
    fun createChatAndNavigate(
        selectedUserIds: List<String>,
        onSuccess: (conversationId: String) -> Unit
    ) {
        if (selectedUserIds.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Phân loại logic: > 1 người là nhóm, 1 người là chat cá nhân
                val isGroup = selectedUserIds.size > 1
                val groupName = if (isGroup) "Nhóm chat mới" else null

                // Gọi API tạo phòng qua ChatRepository
                val result = chatRepository.createConversation(
                    participantIds = selectedUserIds,
                    isGroup = isGroup,
                    name = groupName
                )

                result.onSuccess { newConversationId ->
                    onSuccess(newConversationId)
                }.onFailure { e ->
                    _error.value = e.message ?: "Không thể tạo phòng chat. Vui lòng thử lại!"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Đã xảy ra lỗi hệ thống: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}