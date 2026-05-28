package com.example.se405.android.features.chat_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlin.uuid.ExperimentalUuidApi
import android.util.Log

data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TaskChatViewModel(
    private val chatRepository: ChatRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    // STATES
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var realConversationId: String? = null
    private var pendingParticipantIds: List<String>? = null

    private var messageSubscriptionJob: Job? = null

    private val _chatNameState = MutableStateFlow("")
    val chatNameState: StateFlow<String> = _chatNameState.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<MessageEntity?>(null)
    val replyingToMessage: StateFlow<MessageEntity?> = _replyingToMessage.asStateFlow()

    fun setReplyMessage(message: MessageEntity?) {
        _replyingToMessage.value = message
    }

    // INIT
    fun initChat(idPassedFromNavigation: String, isFromTask: Boolean, pendingIds: String?, chatName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            _chatNameState.value = chatName

            if (!pendingIds.isNullOrBlank()) {
                pendingParticipantIds = pendingIds.split(",")
                realConversationId = null

                _uiState.value = _uiState.value.copy(messages = emptyList(), isLoading = false)
                return@launch
            }

            if (isFromTask) {
                val result = chatRepository.getConversationByTask(idPassedFromNavigation, chatName)
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

    fun renameChat(newName: String) {
        val convId = realConversationId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = chatRepository.renameConversation(convId, newName)

            result.onSuccess {
                _chatNameState.value = newName
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi đổi tên: ${exception.message}",
                    isLoading = false
                )
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun startListeningForMessages(conversationId: String) {
        messageSubscriptionJob?.cancel()
        messageSubscriptionJob = viewModelScope.launch {
            val myUserId = authPreferences.userId.firstOrNull() ?: return@launch
            chatRepository.subscribeToMessages(conversationId, myUserId)
                .catch { e -> Log.e("WebSocketChat", "Lỗi: ${e.message}") }
                .collect { newMessage ->
                    val currentList = _uiState.value.messages
                    if (!currentList.any { it.uuid == newMessage.uuid }) {
                        _uiState.value = _uiState.value.copy(messages = currentList + newMessage)
                    }
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

                startListeningForMessages(conversationId)

            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Lỗi tải tin nhắn",
                    isLoading = false
                )
            }
        }
    }

    // SEND MESSAGE & UPLOAD MEDIA
    @OptIn(ExperimentalUuidApi::class)
    fun sendMessage(content: String, mediaBytes: ByteArray?, mediaType: String?) {
        val currentReplyToId = _replyingToMessage.value?.uuid?.toString()
        viewModelScope.launch {
            _replyingToMessage.value = null
            _uiState.value = _uiState.value.copy(error = null)
            var finalContent = content

            if (realConversationId == null && pendingParticipantIds != null) {
                val isGroupChat = pendingParticipantIds!!.size > 1

                val newConvResult = chatRepository.createConversation(
                    participantIds = pendingParticipantIds!!,
                    isGroup = isGroupChat,
                    name = _chatNameState.value
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

            val result = chatRepository.sendMessage(conversationId = convId, content = finalContent, replyToId = currentReplyToId)

            result.onSuccess {
                sentMessage ->

                val currentList = _uiState.value.messages
                val isDuplicate = currentList.any { it.uuid == sentMessage.uuid }

                if (!isDuplicate) {
                    _uiState.value = _uiState.value.copy(
                        messages = currentList + sentMessage
                    )
                }
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi gửi tin nhắn: ${exception.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageSubscriptionJob?.cancel() // Ngắt kết nối khi thoát màn hình Chat
    }
}