package com.example.se405.android.features.chat_management.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.features.users_management.domain.repository.UserRepository

data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val pinnedMessages: List<MessageEntity> = emptyList()
)

class TaskChatViewModel(
    private val chatRepository: ChatRepository,
    private val authPreferences: AuthPreferences,
    private val userRepository: UserRepository
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

    // 💡 STATE CHO KHO MEDIA
    private val _sharedMediaState = MutableStateFlow<List<MessageEntity>>(emptyList())
    val sharedMediaState: StateFlow<List<MessageEntity>> = _sharedMediaState.asStateFlow()

    private val _sharedLinksState = MutableStateFlow<List<MessageEntity>>(emptyList())
    val sharedLinksState: StateFlow<List<MessageEntity>> = _sharedLinksState.asStateFlow()

    private val _searchResultsState = MutableStateFlow<List<MessageEntity>>(emptyList())
    val searchResultsState: StateFlow<List<MessageEntity>> = _searchResultsState.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _membersState = MutableStateFlow<List<User>>(emptyList())
    private val _mentionSuggestions = MutableStateFlow<List<User>>(emptyList())
    val mentionSuggestions: StateFlow<List<User>> = _mentionSuggestions.asStateFlow()
    private val _showMentionSuggestions = MutableStateFlow(false)
    val showMentionSuggestions: StateFlow<Boolean> = _showMentionSuggestions.asStateFlow()

    private val selectedMentionIds = mutableListOf<String>()

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
                loadMembers(convId)
            }
        }
    }

    private fun loadMembers(conversationId: String) {
        viewModelScope.launch {
            Log.d("MentionDebug", "Đang tải danh sách thành viên cho phòng: $conversationId")
            val result = chatRepository.getConversationMembers(conversationId)
            result.onSuccess { members ->
                _membersState.value = members
                Log.d("MentionDebug", "Đã tải thành công ${members.size} thành viên vào bộ nhớ tạm.")
            }.onFailure { exception ->
                Log.e("MentionDebug", "Lỗi tải danh sách thành viên: ${exception.message}")
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

    // 💡 ĐÃ SỬA: XỬ LÝ EVENT_TYPE TỪ SUBSCRIPTION
    @OptIn(ExperimentalUuidApi::class)
    private fun startListeningForMessages(conversationId: String) {
        messageSubscriptionJob?.cancel()
        messageSubscriptionJob = viewModelScope.launch {
            val myUserId = authPreferences.userId.firstOrNull() ?: return@launch
            chatRepository.subscribeToMessages(conversationId, myUserId)
                .catch { e -> Log.e("WebSocketChat", "Lỗi: ${e.message}") }
                .collect { (eventType, message) ->
                    val currentList = _uiState.value.messages

                    when (eventType) {
                        "CREATED" -> {
                            if (!currentList.any { it.uuid == message.uuid }) {
                                _uiState.value = _uiState.value.copy(messages = currentList + message)
                            }
                        }
                        "REVOKED" -> {
                            val updatedList = currentList.map {
                                if (it.uuid == message.uuid) it.copy(isRevoked = true) else it
                            }
                            _uiState.value = _uiState.value.copy(messages = updatedList)
                            loadPinnedMessages()
                        }
                        "PINNED", "UNPINNED" -> {
                            loadPinnedMessages() // Cập nhật lại list Pinned

                            val updatedList = currentList.map {
                                if (it.uuid == message.uuid) it.copy(isPinned = (eventType == "PINNED")) else it
                            }
                            _uiState.value = _uiState.value.copy(messages = updatedList)
                        }
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

                // 💡 Tự động tải danh sách ghim khi vừa load xong chat
                loadPinnedMessages()
                startListeningForMessages(conversationId)

            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Lỗi tải tin nhắn",
                    isLoading = false
                )
            }
        }
    }

    // =====================================
    // 💡 CÁC HÀM XỬ LÝ GHIM / THU HỒI / MEDIA
    // =====================================

    fun togglePinMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.togglePinMessage(messageId)
        }
    }

    fun revokeMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.revokeMessage(messageId)
        }
    }

    fun loadPinnedMessages() {
        val convId = realConversationId ?: return
        viewModelScope.launch {
            val myUserId = authPreferences.userId.firstOrNull() ?: return@launch
            chatRepository.getPinnedMessages(convId, myUserId).onSuccess { list ->
                _uiState.value = _uiState.value.copy(pinnedMessages = list)
            }
        }
    }

    fun loadSharedMedia() {
        val convId = realConversationId ?: return
        viewModelScope.launch {
            val myUserId = authPreferences.userId.firstOrNull() ?: return@launch
            chatRepository.getSharedMedia(convId, myUserId).onSuccess { list ->
                _sharedMediaState.value = list
            }
        }
    }

    fun loadSharedLinks() {
        val convId = realConversationId ?: return
        viewModelScope.launch {
            val myUserId = authPreferences.userId.firstOrNull() ?: return@launch
            chatRepository.getSharedLinks(convId, myUserId).onSuccess { list ->
                _sharedLinksState.value = list
            }
        }
    }

    fun searchChat(keyword: String) {
        if (keyword.isBlank()) {
            _searchResultsState.value = emptyList()
            return
        }
        val convId = realConversationId ?: return
        viewModelScope.launch {
            val myUserId = authPreferences.userId.firstOrNull() ?: return@launch
            Log.d("SearchDebug", "ViewModel gọi API tìm kiếm...")
            chatRepository.searchMessages(convId, keyword, myUserId)
                .onSuccess { list ->
                    _searchResultsState.value = list
                }
                .onFailure { exception ->
                    Log.e("SearchDebug", "ViewModel nhận lỗi từ Repo: ${exception.message}")
                }
        }
    }

    fun toggleSearchMode(isActive: Boolean) {
        _isSearching.value = isActive
        if (!isActive) _searchResultsState.value = emptyList()
    }

    // SEND MESSAGE & UPLOAD MEDIA
    @OptIn(ExperimentalUuidApi::class)
    fun sendMessage(content: String, mediaBytes: ByteArray?, mediaType: String?, originalFileName: String? = null) {
        val currentReplyToId = _replyingToMessage.value?.uuid?.toString()

        viewModelScope.launch {
            _replyingToMessage.value = null
            _uiState.value = _uiState.value.copy(error = null)

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
            var finalType = "TEXT"
            var finalFileUrl: String? = null
            var finalFileName: String? = null
            var finalFileSize: String? = null

            if (mediaType != null && mediaBytes != null) {
                finalType = mediaType
                val isImage = mediaType == "IMAGE"
                val uploadFileName = originalFileName ?: if (isImage) "image_${System.currentTimeMillis()}.jpg" else "document_${System.currentTimeMillis()}.pdf"

                val uploadResult = chatRepository.uploadFileToCloudinary(
                    fileBytes = mediaBytes,
                    fileName = uploadFileName,
                    isImage = isImage
                )

                uploadResult.onSuccess { secureUrl ->
                    finalFileUrl = secureUrl
                    finalFileName = uploadFileName
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

            val result = chatRepository.sendMessage(
                conversationId = convId,
                content = content,
                replyToId = currentReplyToId,
                type = finalType,
                fileUrl = finalFileUrl,
                fileName = finalFileName,
                fileSize = finalFileSize,
                mentionedUserIds = selectedMentionIds.toList()
            )

            result.onSuccess { sentMessage ->
                selectedMentionIds.clear()
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

    @OptIn(ExperimentalUuidApi::class)
    fun onChatInputChanged(text: String) {
        val lastIndex = text.lastIndexOf('@')

        if (lastIndex != -1 && (lastIndex == 0 || text[lastIndex - 1] == ' ')) {
            val query = text.substring(lastIndex + 1)

            if (!query.contains(" ")) {
                val currentMembers = _membersState.value
                val filtered = currentMembers.filter {
                    it.displayName.contains(query, ignoreCase = true)
                }.toMutableList() // 💡 Chuyển thành MutableList để thêm phần tử

                // 💡 THÊM LOGIC TÌM KIẾM "MỌI NGƯỜI"
                if ("mọi người".contains(query, ignoreCase = true) || "moi nguoi".contains(query, ignoreCase = true)) {
                    val everyoneDummyUser = User(
                        uuid = kotlin.uuid.Uuid.parse("00000000-0000-0000-0000-000000000000"), //id ảo
                        email = "everyone@system.local", username = "everyone", displayName = "Mọi người", avatarUrl = null,
                        passwordHash = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()
                    )
                    filtered.add(0, everyoneDummyUser)
                }

                _mentionSuggestions.value = filtered
                _showMentionSuggestions.value = filtered.isNotEmpty()
                return
            }
        }
        _showMentionSuggestions.value = false
    }

    @OptIn(ExperimentalUuidApi::class)
    fun selectUserMention(user: User) {
        if (user.displayName == "Mọi người") {
            val allIds = _membersState.value.map { it.uuid.toString() }
            allIds.forEach { id ->
                if (!selectedMentionIds.contains(id)) selectedMentionIds.add(id)
            }
        } else {
            // Tag cá nhân
            if (!selectedMentionIds.contains(user.uuid.toString())) {
                selectedMentionIds.add(user.uuid.toString())
            }
        }
        _showMentionSuggestions.value = false
    }

    override fun onCleared() {
        super.onCleared()
        messageSubscriptionJob?.cancel()
    }
}