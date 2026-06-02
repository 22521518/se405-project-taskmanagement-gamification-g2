@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.example.se405.android.features.chat_management.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.se405.android.features.chat_management.presentation.viewmodel.TaskChatViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

import com.example.se405.android.features.chat_management.presentation.viewmodel.ChatUiState
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity

@Composable
fun TaskChatScreen(
    taskId: String,
    taskName: String,
    avatarUrl: String? = null,
    pendingParticipantIds: String? = null,
    isFromTask: Boolean = true,
    onBackClick: () -> Unit,
    viewModel: TaskChatViewModel = koinViewModel()
) {
    val currentChatName by viewModel.chatNameState.collectAsState()

    LaunchedEffect(taskId, pendingParticipantIds) {
        viewModel.initChat(taskId, isFromTask, pendingParticipantIds, taskName)
    }

    val uiState by viewModel.uiState.collectAsState()
    val sharedMedia by viewModel.sharedMediaState.collectAsState()
    val sharedLinks by viewModel.sharedLinksState.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResultsState.collectAsState()
    val replyingTo by viewModel.replyingToMessage.collectAsState()
    val showMentionSuggestions by viewModel.showMentionSuggestions.collectAsState()
    val mentionSuggestions by viewModel.mentionSuggestions.collectAsState()

    TaskChatScreenContent(
        taskName = taskName,
        avatarUrl = avatarUrl,
        currentChatName = currentChatName,
        onBackClick = onBackClick,
        uiState = uiState,
        sharedMedia = sharedMedia,
        sharedLinks = sharedLinks,
        isSearching = isSearching,
        searchResults = searchResults,
        replyingTo = replyingTo,
        showMentionSuggestions = showMentionSuggestions,
        mentionSuggestions = mentionSuggestions,
        onToggleSearchMode = viewModel::toggleSearchMode,
        onSearchChat = viewModel::searchChat,
        onLoadSharedMediaAndLinks = {
            viewModel.loadSharedMedia()
            viewModel.loadSharedLinks()
        },
        onRenameChat = viewModel::renameChat,
        onTogglePinMessage = viewModel::togglePinMessage,
        onRevokeMessage = viewModel::revokeMessage,
        onSetReplyMessage = viewModel::setReplyMessage,
        onSelectUserMention = viewModel::selectUserMention,
        onChatInputChanged = viewModel::onChatInputChanged,
        onSendMessage = { content, mediaBytes, mediaType, fileName ->
            viewModel.sendMessage(content, mediaBytes, mediaType, fileName)
        }
    )
}

@Composable
fun TaskChatScreenContent(
    taskName: String,
    avatarUrl: String?,
    currentChatName: String,
    onBackClick: () -> Unit,
    uiState: ChatUiState,
    sharedMedia: List<MessageEntity>,
    sharedLinks: List<MessageEntity>,
    isSearching: Boolean,
    searchResults: List<MessageEntity>,
    replyingTo: MessageEntity?,
    showMentionSuggestions: Boolean,
    mentionSuggestions: List<User>,
    onToggleSearchMode: (Boolean) -> Unit,
    onSearchChat: (String) -> Unit,
    onLoadSharedMediaAndLinks: () -> Unit,
    onRenameChat: (String) -> Unit,
    onTogglePinMessage: (String) -> Unit,
    onRevokeMessage: (String) -> Unit,
    onSetReplyMessage: (MessageEntity?) -> Unit,
    onSelectUserMention: (User) -> Unit,
    onChatInputChanged: (String) -> Unit,
    onSendMessage: (String, ByteArray?, String?, String?) -> Unit
) {
    val primaryBlue = Color(0xFF2563EB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại", tint = primaryBlue)
                }
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    if (!avatarUrl.isNullOrBlank() && avatarUrl != "null") {
                        GlideImage(model = avatarUrl, contentDescription = "Avatar", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(taskName.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(currentChatName.ifBlank { taskName }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = primaryBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        ChatRoomBodyContent(
            uiState = uiState,
            sharedMedia = sharedMedia,
            sharedLinks = sharedLinks,
            isSearching = isSearching,
            searchResults = searchResults,
            currentChatName = currentChatName,
            replyingTo = replyingTo,
            showMentionSuggestions = showMentionSuggestions,
            mentionSuggestions = mentionSuggestions,
            chatTitleFallback = taskName,
            showActionBar = true,
            modifier = Modifier.weight(1f),
            onToggleSearchMode = onToggleSearchMode,
            onSearchChat = onSearchChat,
            onLoadSharedMediaAndLinks = onLoadSharedMediaAndLinks,
            onRenameChat = onRenameChat,
            onTogglePinMessage = onTogglePinMessage,
            onRevokeMessage = onRevokeMessage,
            onSetReplyMessage = onSetReplyMessage,
            onSelectUserMention = onSelectUserMention,
            onChatInputChanged = onChatInputChanged,
            onSendMessage = onSendMessage
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TaskChatScreenPreview() {
    com.example.se405.android.core.presentation.theme.Android_Theme {
        TaskChatScreenContent(
            taskName = "Fix bugs",
            avatarUrl = null,
            currentChatName = "Fix bugs",
            onBackClick = {},
            uiState = ChatUiState(),
            sharedMedia = emptyList(),
            sharedLinks = emptyList(),
            isSearching = false,
            searchResults = emptyList(),
            replyingTo = null,
            showMentionSuggestions = false,
            mentionSuggestions = emptyList(),
            onToggleSearchMode = {},
            onSearchChat = {},
            onLoadSharedMediaAndLinks = {},
            onRenameChat = {},
            onTogglePinMessage = {},
            onRevokeMessage = {},
            onSetReplyMessage = {},
            onSelectUserMention = {},
            onChatInputChanged = {},
            onSendMessage = { _, _, _, _ -> }
        )
    }
}
