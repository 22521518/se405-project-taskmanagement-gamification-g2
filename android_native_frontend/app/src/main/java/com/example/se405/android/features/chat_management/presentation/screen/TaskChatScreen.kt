@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.chat_management.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.chat_management.presentation.components.ChatInputBar
import com.example.se405.android.features.chat_management.presentation.components.ChatTopBar
import com.example.se405.android.features.chat_management.presentation.components.MessageBubble
import com.example.se405.android.features.chat_management.presentation.viewmodel.TaskChatViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun TaskChatScreen(
    taskId: String,
    taskName: String,
    onBackClick: () -> Unit,
    viewModel: TaskChatViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(taskId) {
        viewModel.loadMessagesForTask(taskId)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChatTopBar(
                taskName = taskName,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ChatInputBar(
                onSendMessage = { content ->
                    viewModel.sendMessage(taskId = taskId, content = content)
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Trạng thái Đang tải
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Trạng thái Lỗi
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "Lỗi",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Đã xảy ra lỗi",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Trạng thái Rỗng (Chưa có tin nhắn nào)
                uiState.messages.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = "Tin nhắn trống",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                        )
                        Text(
                            text = "Chưa có cuộc thảo luận nào",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hãy là người đầu tiên gửi tin nhắn!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Trạng thái Hiển thị dữ liệu
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Khoảng cách đều giữa các bong bóng
                    ) {
                        items(uiState.messages) { message ->
                            val timeString = try {
                                "${message.createdAt.hour}:${String.format("%02d", message.createdAt.minute)}"
                            } catch (e: Exception) {
                                ""
                            }

                            MessageBubble(
                                message = message.content,
                                senderName = message.sender.displayName,
                                isOwnMessage = message.isOwnMessage,
                                time = timeString
                            )
                        }
                    }
                }
            }
        }
    }
}