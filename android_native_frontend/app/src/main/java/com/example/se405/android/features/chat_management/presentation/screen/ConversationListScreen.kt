@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.example.se405.android.features.chat_management.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.se405.android.features.chat_management.presentation.viewmodel.ConversationListViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel = koinViewModel(),
    onNavigateToChat: (conversationId: String, conversationName: String, avatarUrl: String?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNewMessage: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val myUserId = uiState.myUserId ?: ""

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                                // --- LOGIC TÍNH TOÁN DỮ LIỆU HIỂN THỊ CHUẨN ZALO/MESSENGER ---

                                // 1. Tìm thông tin người đối diện (Dựa vào myUserId)
                                val partner = conversation.participants.firstOrNull { it.uuid.toString() != myUserId }
                                    ?: conversation.participants.firstOrNull()

                                // 2. Tên hiển thị
                                val finalDisplayName = if (conversation.type == "DIRECT") {
                                    partner?.displayName ?: "Trò chuyện Cá nhân"
                                } else {
                                    conversation.name ?: "Nhóm chat"
                                }

                                // 3. Avatar hiển thị
                                val finalAvatarUrl = if (conversation.type == "DIRECT") {
                                    partner?.avatarUrl
                                } else {
                                    null // Chat nhóm tạm thời để null để hiện Icon Group
                                }

                                val lastMessagePrefix = if (Math.random() > 0.5) "Bạn" else partner?.displayName?.take(10)
                                val lastMsgContent = "đã gửi một tin nhắn."
                                val timeAgo = " · 12p"

                                ConversationListItem(
                                    displayName = finalDisplayName,
                                    avatarUrl = finalAvatarUrl,
                                    conversationType = conversation.type,
                                    lastMessagePrefix = lastMessagePrefix,
                                    lastMessageContent = lastMsgContent,
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
    onClick: () -> Unit
) {
    val unreadCount = 0

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
                    fontWeight = if (unreadCount > 0) FontWeight.ExtraBold else FontWeight.Bold,
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
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (!lastMessagePrefix.isNullOrBlank() && lastMessagePrefix == "Bạn") {
                    Text(
                        text = "Bạn: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = lastMessageContent + timeAgo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}