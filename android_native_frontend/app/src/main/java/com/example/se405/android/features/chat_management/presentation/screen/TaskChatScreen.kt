@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.example.se405.android.features.chat_management.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.se405.android.features.chat_management.presentation.components.ChatInputBar
import com.example.se405.android.features.chat_management.presentation.components.ChatOptionItem
import com.example.se405.android.features.chat_management.presentation.components.MessageBubble
import com.example.se405.android.features.chat_management.presentation.viewmodel.TaskChatViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

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
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var selectedImageToView by remember { mutableStateOf<String?>(null) }
    var showChatOptions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val primaryBlue = Color(0xFF2563EB)

    LaunchedEffect(taskId, pendingParticipantIds) {
        viewModel.initChat(taskId, isFromTask, pendingParticipantIds, taskName)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // UI: Trình xem ảnh toàn màn hình
    if (selectedImageToView != null) {
        Dialog(
            onDismissRequest = { selectedImageToView = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                GlideImage(
                    model = selectedImageToView,
                    contentDescription = "Zoomed Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { selectedImageToView = null },
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp).statusBarsPadding()
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Đóng", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }

    // UI: Menu tùy chọn chat
    if (showChatOptions) {
        ModalBottomSheet(
            onDismissRequest = { showChatOptions = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    text = "Tùy chọn đoạn chat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                ChatOptionItem(icon = Icons.Rounded.Search, title = "Tìm kiếm trong đoạn chat", baseColor = primaryBlue) {}
                ChatOptionItem(icon = Icons.Rounded.PhotoLibrary, title = "Ảnh, file và liên kết", baseColor = Color(0xFF10B981)) {}
                ChatOptionItem(icon = Icons.Rounded.PushPin, title = "Tin nhắn đã ghim", baseColor = Color(0xFFF59E0B)) {}
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!avatarUrl.isNullOrBlank() && avatarUrl != "null") {
                                GlideImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = taskName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = taskName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = primaryBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showChatOptions = true }) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Tùy chọn")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = Color.Unspecified,
                    titleContentColor = primaryBlue,
                    navigationIconContentColor = primaryBlue,
                    actionIconContentColor = primaryBlue
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                onSendMessage = { content, mediaUri, mediaType ->
                    val mediaBytes = mediaUri?.let { uri -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }
                    viewModel.sendMessage(content, mediaBytes, mediaType)
                }
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryBlue)
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = "Lỗi", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp).padding(bottom = 8.dp))
                        Text("Đã xảy ra lỗi", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(uiState.error ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp), textAlign = TextAlign.Center)
                    }
                }
                uiState.messages.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = "Trống", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(64.dp).padding(bottom = 16.dp))
                        Text("Chưa có cuộc thảo luận nào", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Hãy là người đầu tiên gửi tin nhắn!", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.messages) { message ->
                            val timeString = try {
                                "${message.createdAt.hour}:${String.format("%02d", message.createdAt.minute)}"
                            } catch (e: Exception) { "" }

                            MessageBubble(
                                message = message.content,
                                senderName = message.sender.displayName,
                                isOwnMessage = message.isOwnMessage,
                                senderAvatarUrl = message.sender.avatarUrl,
                                time = timeString,
                                onImageClick = { clickedUrl ->
                                    selectedImageToView = clickedUrl
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}