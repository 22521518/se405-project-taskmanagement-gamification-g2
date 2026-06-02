@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.example.se405.android.features.chat_management.presentation.screen

import androidx.compose.foundation.background
import java.time.LocalDateTime
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.se405.android.core.presentation.components.AppHeader
import com.example.se405.android.core.utils.formatTimeAgo
import com.example.se405.android.features.chat_management.presentation.viewmodel.ConversationListViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

import com.example.se405.android.features.chat_management.presentation.viewmodel.ConversationListUiState
import com.example.se405.android.features.chat_management.domain.entity.Conversation
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.users_management.domain.entity.User
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel = koinViewModel(),
    onNavigateToChat: (conversationId: String, conversationName: String, avatarUrl: String?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNewMessage: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    ConversationListScreenContent(
        uiState = uiState,
        onNavigateToChat = onNavigateToChat,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToNewMessage = onNavigateToNewMessage
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ConversationListScreenContent(
    uiState: ConversationListUiState,
    onNavigateToChat: (conversationId: String, conversationName: String, avatarUrl: String?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNewMessage: () -> Unit,
) {
    val myUserId = uiState.myUserId ?: ""

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppHeader(
                title = "Messages",
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            modifier = Modifier.size(26.dp),
                            contentDescription = "Tìm kiếm",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onNavigateToNewMessage) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Tin nhắn mới",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewMessage,
                containerColor = MaterialTheme.colorScheme.primary,
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
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

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

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
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
                                // 1. Tìm thông tin người đối diện (Dựa vào myUserId)
                                val partner = conversation.participants.firstOrNull { it.uuid.toString() != myUserId }
                                    ?: conversation.participants.firstOrNull()

                                // 2. Tên hiển thị
                                val finalDisplayName = when (conversation.type) {
                                    "DIRECT" -> partner?.displayName ?: "Trò chuyện Cá nhân"
                                    "TASK" -> conversation.name ?: "Thảo luận công việc"
                                    else -> conversation.name ?: "Nhóm chat mới"
                                }

                                // 3. Avatar hiển thị
                                val finalAvatarUrl = if (conversation.type == "DIRECT") {
                                    partner?.avatarUrl
                                } else {
                                    null
                                }

                                val lastMsg = conversation.lastMessage

                                val lastMessagePrefix = when {
                                    lastMsg == null -> ""
                                    lastMsg.senderId == myUserId -> "Bạn: "
                                    conversation.type == "GROUP" -> "${lastMsg.senderName}: "
                                    else -> ""
                                }

                                val rawContent = lastMsg?.content ?: "Chưa có tin nhắn"
                                val lastMsgContent = if (rawContent.contains("[IMAGE:") && rawContent.endsWith("]")) {
                                    "[Hình ảnh]"
                                } else {
                                    rawContent
                                }

                                val timeAgo = lastMsg?.createdAt?.let { " · ${formatTimeAgo(it)}" } ?: ""
                                val isMessageUnread = lastMsg != null && lastMsg.senderId.lowercase() != myUserId.lowercase()

                                ConversationListItem(
                                    displayName = finalDisplayName,
                                    avatarUrl = finalAvatarUrl,
                                    conversationType = conversation.type,
                                    lastMessagePrefix = lastMessagePrefix,
                                    lastMessageContent = lastMsgContent,
                                    isUnread = isMessageUnread,
                                    timeAgo = timeAgo,
                                    onClick = {
                                        onNavigateToChat(conversation.uuid, finalDisplayName, finalAvatarUrl)
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

@OptIn(ExperimentalUuidApi::class)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ConversationListScreenPreview() {
    com.example.se405.android.core.presentation.theme.Android_Theme {
        ConversationListScreenContent(
            uiState = ConversationListUiState(
                conversations = listOf(
                    Conversation(
                        uuid = "1",
                        type = "DIRECT",
                        name = null,
                        taskUuid = null,
                        participants = listOf(
                            User(uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"), email = "test@example.com", username = "user1", displayName = "John Doe", avatarUrl = null)
                        ),
                        lastMessage = com.example.se405.android.features.chat_management.domain.entity.LastMessageInfo(
                            content = "Hello there!",
                            createdAt = LocalDateTime.now().minusMinutes(5),
                            senderId = "00000000-0000-0000-0000-000000000001",
                            senderName = "John Doe"
                        )
                    ),
                    Conversation(
                        uuid = "2",
                        type = "GROUP",
                        name = "Project Team",
                        taskUuid = null,
                        participants = emptyList(),
                        lastMessage = null
                    )
                ),
                isLoading = false,
                error = null,
                myUserId = "myId"
            ),
            onNavigateToChat = { _, _, _ -> },
            onNavigateToSearch = {},
            onNavigateToNewMessage = {}
        )
    }
}

// --- COMPONENT: UI ITEM ZALO / MESSENGER STYLE ---
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ConversationListItem(
    displayName: String,
    avatarUrl: String?,
    conversationType: String,
    lastMessagePrefix: String?,
    lastMessageContent: String,
    timeAgo: String,
    isUnread: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- KHỐI AVATAR ---
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    when (conversationType) {
                        "TASK" -> MaterialTheme.colorScheme.secondaryContainer
                        "GROUP" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                GlideImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val icon = when (conversationType) {
                    "TASK" -> Icons.Rounded.ChatBubbleOutline
                    "GROUP" -> Icons.Rounded.Group
                    else -> Icons.Rounded.PersonOutline
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = when (conversationType) {
                        "TASK" -> MaterialTheme.colorScheme.onSecondaryContainer
                        "GROUP" -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // --- KHỐI NỘI DUNG ---
        Column(modifier = Modifier.weight(1f)) {
            // Dòng trên: Tên + Label Task
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (conversationType == "TASK") {
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

            // Dòng dưới: Tin nhắn cuối cùng (Bạn: Nội dung · 12p)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!lastMessagePrefix.isNullOrBlank() && conversationType != "DIRECT") {
                    Text(
                        text = "$lastMessagePrefix: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (!lastMessagePrefix.isNullOrBlank() && lastMessagePrefix == "Bạn") {
                    Text(
                        text = "Bạn: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = lastMessageContent + timeAgo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}