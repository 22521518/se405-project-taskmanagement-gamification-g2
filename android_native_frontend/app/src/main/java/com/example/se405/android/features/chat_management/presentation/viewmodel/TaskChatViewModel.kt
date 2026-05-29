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
    fun sendMessage(content: String, mediaBytes: ByteArray?, mediaType: String?, originalFileName: String? = null) {
        val currentReplyToId = _replyingToMessage.value?.uuid?.toString()

        viewModelScope.launch {
            // 1. Reset state hiển thị UI
            _replyingToMessage.value = null
            _uiState.value = _uiState.value.copy(error = null)

            // 2. Tạo phòng chat nếu chưa tồn tại
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

            // 3. Khởi tạo Metadata mặc định
            var finalType = "TEXT"
            var finalFileUrl: String? = null
            var finalFileName: String? = null
            var finalFileSize: String? = null

            // 4. Xử lý Upload Media/File nếu có
            if (mediaType != null && mediaBytes != null) {
                finalType = mediaType // "IMAGE" hoặc "FILE"
                val isImage = mediaType == "IMAGE"

                // Lấy tên file gốc hoặc tự tạo tên nếu Client không truyền vào
                val uploadFileName = originalFileName ?: if (isImage) {
                    "image_${System.currentTimeMillis()}.jpg"
                } else {
                    "document_${System.currentTimeMillis()}.pdf"
                }

                val uploadResult = chatRepository.uploadFileToCloudinary(
                    fileBytes = mediaBytes,
                    fileName = uploadFileName,
                    isImage = isImage
                )

                uploadResult.onSuccess { secureUrl ->
                    finalFileUrl = secureUrl
                    finalFileName = uploadFileName

                    // Tự động tính toán dung lượng File sang đơn vị chuẩn (KB, MB)
                    val kb = mediaBytes.size / 1024.0
                    val mb = kb / 1024.0
                    finalFileSize = when {
                        mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
                        kb >= 1.0 -> String.format(java.util.Locale.US, "%.1f KB", kb)
                        else -> "${mediaBytes.size} Bytes"
                    }
                }.onFailure {
                    _uiState.value = _uiState.value.copy(error = "Không thể tải tệp lên máy chủ!")
                    return@launch
                }
            }

            // 5. Gửi dữ liệu đồng bộ lên GraphQL Server
            val result = chatRepository.sendMessage(
                conversationId = convId,
                content = content,
                replyToId = currentReplyToId,
                type = finalType,
                fileUrl = finalFileUrl,
                fileName = finalFileName,
                fileSize = finalFileSize
            )

            // 6. Cập nhật UI nếu thành công
            result.onSuccess { sentMessage ->
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