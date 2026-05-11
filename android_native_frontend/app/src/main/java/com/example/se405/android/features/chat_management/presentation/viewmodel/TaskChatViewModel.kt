@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.chat_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TaskChatViewModel(
    // 1. Tiêm ChatRepository (đã nối Apollo GraphQL) vào đây
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // 2. Hàm lấy tin nhắn thật từ Database (Gọi ở LaunchedEffect bên UI)
    fun loadMessagesForTask(taskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Vì Repo trả về Flow, chúng ta dùng collect để hứng dữ liệu
                chatRepository.getMessagesByTask(taskId).collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Lỗi không xác định",
                    isLoading = false
                )
            }
        }
    }

    // 3. Hàm gửi tin nhắn thật lên Backend (Đã thêm tham số taskId)
    fun sendMessage(taskId: String, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val result = chatRepository.sendMessage(taskId = taskId, content = content)

            result.onSuccess {
                // Khi gửi thành công lên Backend, gọi lại hàm load để lấy danh sách mới nhất về UI
                loadMessagesForTask(taskId)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi gửi tin nhắn: ${exception.message}"
                )
            }
        }
    }
}