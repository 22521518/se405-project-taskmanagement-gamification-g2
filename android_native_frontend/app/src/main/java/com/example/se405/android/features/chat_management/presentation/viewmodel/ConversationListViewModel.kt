package com.example.se405.android.features.chat_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.features.chat_management.domain.entity.Conversation
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import com.example.se405.android.features.users_management.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

// Trạng thái của màn hình Inbox
data class ConversationListUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val myUserId: String? = null
)

class ConversationListViewModel(
    private val chatRepository: ChatRepository,
    private val authPrefs: AuthPreferences,
    val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    init {
        startPollingConversations()
    }

    private fun startPollingConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val myId = authPrefs.userId.firstOrNull() ?: ""

            while (isActive) {
                try {
                    val list = chatRepository.getMyConversations().first()

                    _uiState.value = _uiState.value.copy(
                        conversations = list,
                        myUserId = myId,
                        isLoading = false
                    )
                } catch (e: Exception) {
                    if (_uiState.value.conversations.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            error = e.message ?: "Không thể tải danh sách tin nhắn",
                            isLoading = false
                        )
                    }
                }

                kotlinx.coroutines.delay(2000)
            }
        }
    }
}