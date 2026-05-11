@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.chat_management.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.components.formatDate
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

    // 1. Tự động gọi API lấy tin nhắn khi vào màn hình với taskId tương ứng
    LaunchedEffect(taskId) {
        viewModel.loadMessagesForTask(taskId)
    }

    // 2. Tự động cuộn xuống tin nhắn mới nhất khi có tin nhắn mới thêm vào
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
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
        // Nền tổng thể của ứng dụng
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F8FF)) // Màu nền xanh dương nhạt cực nhẹ
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
                    Text(
                        text = "Đã xảy ra lỗi: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = AppText.Body2Regular,
                        textAlign = TextAlign.Center
                    )
                }

                // Trạng thái Rỗng (Chưa có tin nhắn nào)
                uiState.messages.isEmpty() -> {
                    Text(
                        text = "Chưa có cuộc thảo luận nào cho Task này.\nHãy là người đầu tiên gửi tin nhắn!",
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        style = AppText.Body2Regular,
                        textAlign = TextAlign.Center
                    )
                }

                // Trạng thái Hiển thị dữ liệu
                else -> {
                    LazyColumn(
                        state = listState, // Gắn state để hỗ trợ tự động cuộn
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(uiState.messages) { message ->
                            // Định dạng thời gian hiển thị (Giả định bạn có hàm chuyển đổi ở core/components)
                            val timeString = try {
                                // Tạm thời dùng toString của LocalDateTime, sau này bạn có thể dùng format
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