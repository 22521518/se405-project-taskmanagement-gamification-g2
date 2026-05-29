@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.example.se405.android.features.chat_management.presentation.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.automirrored.rounded.Reply
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

// Hàm hỗ trợ lấy tên file thật từ thiết bị
private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) result = cursor.getString(index)
            }
        }
    }
    return result ?: uri.path?.substringAfterLast('/')
}

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
    val currentChatName by viewModel.chatNameState.collectAsState()
    val replyingTo by viewModel.replyingToMessage.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var selectedImageToView by remember { mutableStateOf<String?>(null) }
    var showChatOptions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRenameDialog by remember { mutableStateOf(false) }
    var newChatNameInput by remember { mutableStateOf("") }

    val primaryBlue = Color(0xFF2563EB)

    LaunchedEffect(taskId, pendingParticipantIds) {
        viewModel.initChat(taskId, isFromTask, pendingParticipantIds, taskName)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

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

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Đổi tên nhóm chat", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newChatNameInput,
                    onValueChange = { newChatNameInput = it },
                    placeholder = { Text("Nhập tên mới...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newChatNameInput.isNotBlank()) {
                            viewModel.renameChat(newChatNameInput.trim())
                        }
                        showRenameDialog = false
                    }
                ) { Text("Lưu", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB)) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Hủy", color = Color.Gray) }
            }
        )
    }

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

                ChatOptionItem(
                    icon = Icons.Rounded.Edit,
                    title = "Đổi tên nhóm",
                    baseColor = Color(0xFF8B5CF6)
                ) {
                    showChatOptions = false
                    newChatNameInput = currentChatName.ifBlank { taskName }
                    showRenameDialog = true
                }

                ChatOptionItem(icon = Icons.Rounded.Search, title = "Tìm kiếm trong đoạn chat", baseColor = primaryBlue) {}
                ChatOptionItem(icon = Icons.Rounded.PhotoLibrary, title = "Ảnh, file và liên kết", baseColor = Color(0xFF10B981)) {}
                ChatOptionItem(icon = Icons.Rounded.PushPin, title = "Tin nhắn đã ghim", baseColor = Color(0xFFF59E0B)) {}
            }
        }
    }


    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        //contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
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
                            text = currentChatName.ifBlank { taskName },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
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
                    replyingTo = replyingTo,
                    onCancelReply = { viewModel.setReplyMessage(null) },
                    onSendMessage = { content, mediaUri, mediaType ->
                        val mediaBytes = mediaUri?.let { uri ->
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        }
                        val fileName = mediaUri?.let { getFileNameFromUri(context, it) }

                        viewModel.sendMessage(content, mediaBytes, mediaType, fileName)
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
                                "${message.createdAt.hour}:${
                                    String.format(
                                        "%02d",
                                        message.createdAt.minute
                                    )
                                }"
                            } catch (e: Exception) {
                                ""
                            }

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if ((message.isOwnMessage && dismissValue == SwipeToDismissBoxValue.EndToStart) ||
                                        (!message.isOwnMessage && dismissValue == SwipeToDismissBoxValue.StartToEnd)) {
                                        viewModel.setReplyMessage(message)
                                    }
                                    false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = !message.isOwnMessage,
                                enableDismissFromEndToStart = message.isOwnMessage,
                                backgroundContent = {
                                    val isSwiping = (message.isOwnMessage && (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart || dismissState.currentValue == SwipeToDismissBoxValue.EndToStart)) ||
                                            (!message.isOwnMessage && (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd || dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd))

                                    if (isSwiping) {
                                        val color by animateColorAsState(
                                            targetValue = primaryBlue.copy(alpha = 0.15f), label = ""
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color)
                                                .padding(horizontal = 16.dp),
                                            contentAlignment = if (message.isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.Reply,
                                                contentDescription = "Trả lời",
                                                tint = primaryBlue
                                            )
                                        }
                                    }
                                }
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                                    MessageBubble(
                                        message = message.content,
                                        senderName = message.sender.displayName,
                                        isOwnMessage = message.isOwnMessage,
                                        senderAvatarUrl = message.sender.avatarUrl,
                                        time = timeString,
                                        type = message.type,
                                        fileUrl = message.fileUrl,
                                        fileName = message.fileName,
                                        fileSize = message.fileSize,
                                        replyTo = message.replyTo,
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
    }
}