package com.example.se405.android.features.chat_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TaskChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Biến lưu trữ ID thật của phòng chat (Tất cả mọi thao tác đều dùng ID này)
    private var realConversationId: String? = null

    // Hàm khởi tạo phòng chat (Gọi ở LaunchedEffect bên UI)
    fun initChat(idPassedFromNavigation: String, isFromTask: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            if (isFromTask) {
                // ĐI TỪ MÀN HÌNH TASK SANG: Gọi API đổi taskId lấy conversationId
                val result = chatRepository.getConversationByTask(idPassedFromNavigation)
                result.onSuccess { convId ->
                    realConversationId = convId
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Lỗi tải phòng chat: ${exception.message}",
                        isLoading = false
                    )
                    return@launch
                }
            } else {
                // ĐI TỪ INBOX / NEW CHAT SANG: ID truyền vào chính là ID phòng chat
                realConversationId = idPassedFromNavigation
            }

            // Có ID phòng thật rồi thì tiến hành tải tin nhắn
            realConversationId?.let { convId ->
                loadMessages(convId)
            }
        }
    }

    private fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            val result = chatRepository.getMessagesByConversation(conversationId)
            result.onSuccess { messages ->
                _uiState.value = _uiState.value.copy(
                    messages = messages,
                    isLoading = false
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Lỗi tải tin nhắn",
                    isLoading = false
                )
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        // Lấy ID phòng chat hiện tại để gửi
        val convId = realConversationId ?: return

        viewModelScope.launch {
            val result = chatRepository.sendMessage(conversationId = convId, content = content)

            result.onSuccess {
                // Gửi thành công thì load lại danh sách tin nhắn
                loadMessages(convId)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi gửi tin nhắn: ${exception.message}"
                )
            }
        }
    }
}