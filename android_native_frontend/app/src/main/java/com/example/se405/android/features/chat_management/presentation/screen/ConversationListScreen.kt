@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.se405.android.features.chat_management.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.se405.android.features.chat_management.domain.entity.Conversation
import com.example.se405.android.features.chat_management.presentation.viewmodel.ConversationListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel = koinViewModel(),
    onNavigateToChat: (conversationId: String, conversationName: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNewMessage: () -> Unit,
    onBackClick: () -> Unit
) {
    // 1. Lấy dữ liệu THẬT từ CSDL thông qua ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Messages",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Tìm kiếm",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onNavigateToNewMessage) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = "Tin nhắn mới",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewMessage,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tạo hội thoại mới",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Đang tải dữ liệu
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Lỗi khi gọi API
                uiState.error != null -> {
                    Text(
                        text = "Lỗi tải dữ liệu: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Thành công, có dữ liệu
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // --- PHẦN 1: SUGGESTED CONTACTS ---
                        // (Ghi chú: Tạm thời ẩn phần này hoặc để trống vì Backend chưa có API lấy danh sách User gợi ý)
                        /* item {
                            Text(
                                text = "Suggested",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
                            )
                            // TODO: Gọi API getUsers để hiển thị LazyRow ở đây
                        }
                        */

                        // --- PHẦN 2: DANH SÁCH CHAT GẦN ĐÂY ---
                        item {
                            Text(
                                text = "Recent",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 8.dp)
                            )
                        }

                        if (uiState.conversations.isEmpty()) {
                            item {
                                Text(
                                    text = "Bạn chưa có đoạn chat nào.",
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(uiState.conversations) { conversation ->
                                ConversationListItem(
                                    conversation = conversation,
                                    onClick = {
                                        val displayName = conversation.name ?: if(conversation.type == "TASK") "Thảo luận Task" else "Người dùng"
                                        onNavigateToChat(conversation.uuid, displayName)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENT: ITEM CONVERSATION KẾT NỐI VỚI DỮ LIỆU THẬT ---
@Composable
fun ConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    // Tạm thời giả định giá trị unread (Chờ Backend cập nhật API)
    val unreadCount = 0

    // Tên hiển thị
    val displayName = conversation.name ?: when (conversation.type) {
        "TASK" -> "Thảo luận Task"
        "GROUP" -> "Trò chuyện Nhóm"
        else -> "Trò chuyện Cá nhân"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Khối Avatar thông minh
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    when (conversation.type) {
                        "TASK" -> MaterialTheme.colorScheme.secondaryContainer
                        "GROUP" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (conversation.type) {
                "TASK" -> Icons.Rounded.ChatBubbleOutline
                "GROUP" -> Icons.Rounded.Group
                else -> Icons.Rounded.PersonOutline
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = when (conversation.type) {
                    "TASK" -> MaterialTheme.colorScheme.onSecondaryContainer
                    "GROUP" -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Khối hiển thị Nội dung
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (unreadCount > 0) FontWeight.ExtraBold else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (conversation.type == "TASK") {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "Task",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Nhấn để xem tin nhắn...",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                color = if (unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}