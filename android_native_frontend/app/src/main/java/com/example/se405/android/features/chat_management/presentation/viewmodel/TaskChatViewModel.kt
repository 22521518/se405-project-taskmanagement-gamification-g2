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

    // STATES
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var realConversationId: String? = null
    private var pendingParticipantIds: List<String>? = null
    private var pendingChatName: String = ""

    // INIT
    fun initChat(idPassedFromNavigation: String, isFromTask: Boolean, pendingIds: String?, chatName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            if (!pendingIds.isNullOrBlank()) {
                pendingParticipantIds = pendingIds.split(",")
                pendingChatName = chatName
                realConversationId = null

                _uiState.value = _uiState.value.copy(messages = emptyList(), isLoading = false)
                return@launch
            }

            if (isFromTask) {
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
                realConversationId = idPassedFromNavigation
            }

            realConversationId?.let { convId ->
                loadMessages(convId)
            }
        }
    }

    // LOAD MESSAGES
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

    // SEND MESSAGE & UPLOAD MEDIA
    fun sendMessage(content: String, mediaBytes: ByteArray?, mediaType: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            var finalContent = content

            if (realConversationId == null && pendingParticipantIds != null) {
                val isGroupChat = pendingParticipantIds!!.size > 1

                val newConvResult = chatRepository.createConversation(
                    participantIds = pendingParticipantIds!!,
                    isGroup = isGroupChat,
                    name = pendingChatName
                )

                newConvResult.onSuccess { newConvId ->
                    realConversationId = newConvId
                    pendingParticipantIds = null
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(error = "Lỗi tạo phòng: ${exception.message}")
                    return@launch
                }
            }

            val convId = realConversationId ?: return@launch

            if (mediaType == "IMAGE" && mediaBytes != null) {
                val uploadResult = chatRepository.uploadImageToCloudinary(mediaBytes)

                uploadResult.onSuccess { secureUrl ->
                    finalContent = if (finalContent.isNotBlank()) {
                        "$finalContent\n[IMAGE:$secureUrl]"
                    } else {
                        "[IMAGE:$secureUrl]"
                    }
                }.onFailure {
                    _uiState.value = _uiState.value.copy(error = "Không thể tải ảnh lên máy chủ!")
                    return@launch
                }
            }

            val result = chatRepository.sendMessage(conversationId = convId, content = finalContent)

            result.onSuccess {
                loadMessages(convId)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi gửi tin nhắn: ${exception.message}"
                )
            }
        }
    }
}