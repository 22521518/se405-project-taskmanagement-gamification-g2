@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.chat_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.users_management.domain.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TaskChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Tạo một User giả để test giao diện
    private val mockMe = User(
        uuid = Uuid.random(),
        email = "me@example.com",
        username = "my_user",
        passwordHash = "hash",
        displayName = "Tôi",
        avatarUrl = "",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private val mockOther = User(
        uuid = Uuid.random(),
        email = "other@example.com",
        username = "other_user",
        passwordHash = "hash",
        displayName = "Trần B",
        avatarUrl = "",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    init {
        loadDummyMessages()
    }

    private fun loadDummyMessages() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        val dummy = listOf(
            MessageEntity(
                uuid = Uuid.random(),
                content = "Chào team, dự án đến đâu rồi?",
                taskUuid = Uuid.random(),
                sender = mockOther,
                createdAt = LocalDateTime.now().minusMinutes(10),
                isOwnMessage = false
            ),
            MessageEntity(
                uuid = Uuid.random(),
                content = "Mình đang làm phần giao diện Chat nhé!",
                taskUuid = Uuid.random(),
                sender = mockMe,
                createdAt = LocalDateTime.now().minusMinutes(2),
                isOwnMessage = true
            )
        )
        _uiState.value = _uiState.value.copy(messages = dummy, isLoading = false)
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            val newMessage = MessageEntity(
                uuid = Uuid.random(),
                content = content,
                taskUuid = Uuid.random(), // Thực tế sẽ lấy từ tham số truyền vào
                sender = mockMe,
                createdAt = LocalDateTime.now(),
                isOwnMessage = true
            )
            val currentList = _uiState.value.messages.toMutableList()
            currentList.add(newMessage)
            _uiState.value = _uiState.value.copy(messages = currentList)
        }
    }
}