@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.example.se405.android.features.chat_management.presentation.screen

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.se405.android.features.chat_management.presentation.components.ChatInputBar
import com.example.se405.android.features.chat_management.presentation.components.ChatOptionItem
import com.example.se405.android.features.chat_management.presentation.components.DateDivider
import com.example.se405.android.features.chat_management.presentation.components.MessageBubble
import com.example.se405.android.features.chat_management.presentation.components.PinnedMessagesBanner
import com.example.se405.android.features.chat_management.presentation.components.SharedMediaView
import com.example.se405.android.features.chat_management.presentation.viewmodel.TaskChatViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi
import com.example.se405.android.features.chat_management.presentation.viewmodel.ChatUiState
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity

/**
 * Tab Chat dùng cho màn hình chi tiết Workspace — phòng chat get-or-create theo workspaceId,
 * tự động thêm thành viên workspace. Tái sử dụng toàn bộ thân chat [ChatRoomBody].
 */
@Composable
fun WorkspaceChatRoom(workspaceId: String, workspaceName: String, modifier: Modifier = Modifier) {
    val viewModel: TaskChatViewModel = koinViewModel()
    LaunchedEffect(workspaceId) { viewModel.initWorkspaceChat(workspaceId, workspaceName) }
    ChatRoomBody(viewModel = viewModel, chatTitleFallback = workspaceName, showActionBar = true, modifier = modifier)
}

/**
 * Tab Chat dùng cho màn hình chi tiết Project — phòng chat get-or-create theo projectId,
 * tự động thêm thành viên project. Tái sử dụng toàn bộ thân chat [ChatRoomBody].
 */
@Composable
fun ProjectChatRoom(projectId: String, projectName: String, modifier: Modifier = Modifier) {
    val viewModel: TaskChatViewModel = koinViewModel()
    LaunchedEffect(projectId) { viewModel.initProjectChat(projectId, projectName) }
    ChatRoomBody(viewModel = viewModel, chatTitleFallback = projectName, showActionBar = true, modifier = modifier)
}

internal fun getFileNameFromUri(context: Context, uri: Uri): String? {
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

private val PrimaryBlue = Color(0xFF2563EB)

/**
 * Phần thân tái sử dụng của khung chat (danh sách tin nhắn, thanh nhập, ghim, mention,
 * tìm kiếm và các dialog kho lưu trữ). Dùng chung cho [TaskChatScreen] và cho tab Chat
 * của Workspace/Project — không bao gồm thanh tiêu đề riêng nên không bị chồng header.
 *
 * @param showActionBar hiển thị thanh hành động gọn (tìm kiếm + tùy chọn) ở đầu thân chat.
 *        Bật cho tab Workspace/Project (vốn không có top bar chat riêng).
 */


@SuppressLint("DefaultLocale")
@Composable
fun ChatRoomBody(
    viewModel: TaskChatViewModel,
    chatTitleFallback: String,
    modifier: Modifier = Modifier,
    showActionBar: Boolean = true,
) {
    val uiState by viewModel.uiState.collectAsState()
    val sharedMedia by viewModel.sharedMediaState.collectAsState()
    val sharedLinks by viewModel.sharedLinksState.collectAsState()

    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResultsState.collectAsState()

    val currentChatName by viewModel.chatNameState.collectAsState()
    val replyingTo by viewModel.replyingToMessage.collectAsState()
    
    val showMentionSuggestions by viewModel.showMentionSuggestions.collectAsState()
    val mentionSuggestions by viewModel.mentionSuggestions.collectAsState()

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
        chatTitleFallback = chatTitleFallback,
        modifier = modifier,
        showActionBar = showActionBar,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@SuppressLint("DefaultLocale")
@Composable
fun ChatRoomBodyContent(
    uiState: ChatUiState,
    sharedMedia: List<MessageEntity>,
    sharedLinks: List<MessageEntity>,
    isSearching: Boolean,
    searchResults: List<MessageEntity>,
    currentChatName: String,
    replyingTo: MessageEntity?,
    showMentionSuggestions: Boolean,
    mentionSuggestions: List<User>,
    chatTitleFallback: String,
    modifier: Modifier = Modifier,
    showActionBar: Boolean = true,
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
    var searchKeyword by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageToView by remember { mutableStateOf<String?>(null) }
    var showChatOptions by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showSharedMedia by remember { mutableStateOf(false) }
    var newChatNameInput by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val mediaSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // ─── Dialogs & bottom sheets ───────────────────────────────
    if (selectedImageToView != null) {
        Dialog(
            onDismissRequest = { selectedImageToView = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                GlideImage(model = selectedImageToView, contentDescription = "Zoomed", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                IconButton(onClick = { selectedImageToView = null }, modifier = Modifier.align(Alignment.TopStart).padding(16.dp).statusBarsPadding()) {
                    Icon(Icons.Rounded.Close, contentDescription = "Đóng", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }

    if (showChatOptions) {
        ModalBottomSheet(onDismissRequest = { showChatOptions = false }, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.primary) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Tùy chọn đoạn chat", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp), color = Color.Black)
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                ChatOptionItem(icon = Icons.Rounded.Edit, title = "Đổi tên nhóm", baseColor = Color(0xFF8B5CF6)) {
                    showChatOptions = false
                    newChatNameInput = currentChatName.ifBlank { chatTitleFallback }
                    showRenameDialog = true
                }
                ChatOptionItem(icon = Icons.Rounded.Search, title = "Tìm kiếm trong đoạn chat", baseColor = PrimaryBlue) {
                    showChatOptions = false
                    onToggleSearchMode(true)
                }
                ChatOptionItem(icon = Icons.Rounded.PhotoLibrary, title = "Ảnh, file và liên kết", baseColor = Color(0xFF10B981)) {
                    showChatOptions = false
                    showSharedMedia = true
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Đổi tên đoạn chat") },
            text = {
                OutlinedTextField(
                    value = newChatNameInput,
                    onValueChange = { newChatNameInput = it },
                    singleLine = true,
                    placeholder = { Text("Nhập tên mới...") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newChatNameInput.isNotBlank()) onRenameChat(newChatNameInput.trim())
                    showRenameDialog = false
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Hủy") } }
        )
    }

    if (showSharedMedia) {
        LaunchedEffect(Unit) {
            onLoadSharedMediaAndLinks()
        }
        ModalBottomSheet(
            onDismissRequest = { showSharedMedia = false },
            sheetState = mediaSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxHeight(0.92f)
        ) {
            val listAnh = sharedMedia.filter { it.type == "IMAGE" }
            val listFile = sharedMedia.filter { it.type == "FILE" }
            SharedMediaView(images = listAnh, files = listFile, links = sharedLinks, onImageClick = { selectedImageToView = it })
        }
    }

    // ─── Thân chat ─────────────────────────────────────────────
    Column(modifier = modifier.fillMaxSize()) {

        if (showActionBar) {
            if (isSearching) {
                Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            onToggleSearchMode(false)
                            searchKeyword = ""
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại", tint = PrimaryBlue)
                        }
                        OutlinedTextField(
                            value = searchKeyword,
                            onValueChange = {
                                searchKeyword = it
                                onSearchChat(it)
                            },
                            placeholder = { Text("Nhập từ khóa...", color = Color.Gray, fontSize = 15.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(25.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
                                focusedContainerColor = Color(0xFFF1F5F9),
                                unfocusedContainerColor = Color(0xFFF1F5F9)
                            ),
                            modifier = Modifier.weight(1f).height(50.dp).padding(end = 12.dp).clip(RoundedCornerShape(25.dp))
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onToggleSearchMode(true) }) {
                        Icon(Icons.Rounded.Search, contentDescription = "Tìm kiếm", tint = PrimaryBlue)
                    }
                    IconButton(onClick = { showChatOptions = true }) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Tùy chọn", tint = PrimaryBlue)
                    }
                }
            }
        }

        if (isSearching) {
            ChatSearchResults(
                searchKeyword = searchKeyword,
                searchResults = searchResults,
                modifier = Modifier.weight(1f),
                onResultClick = { msg ->
                    onToggleSearchMode(false)
                    searchKeyword = ""
                    val targetIndex = uiState.messages.indexOfFirst { it.uuid == msg.uuid }
                    if (targetIndex != -1) coroutineScope.launch { listState.animateScrollToItem(targetIndex) }
                    else Toast.makeText(context, "Tin nhắn cũ chưa được tải", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            PinnedMessagesBanner(pinnedMessages = uiState.pinnedMessages, onMessageClick = { pinnedMsg ->
                val targetIndex = uiState.messages.indexOfFirst { it.uuid == pinnedMsg.uuid }
                if (targetIndex != -1) coroutineScope.launch { listState.animateScrollToItem(targetIndex) }
            })
            if (uiState.pinnedMessages.isNotEmpty()) HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
                    uiState.messages.isEmpty() -> {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = "Trống", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Text("Chưa có cuộc thảo luận nào", fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(uiState.messages, key = { _, msg -> msg.uuid.toString() }) { index, message ->
                                val currentMessageDate = message.createdAt.toLocalDate()
                                if (index == 0 || uiState.messages[index - 1].createdAt.toLocalDate() != currentMessageDate) {
                                    DateDivider(date = currentMessageDate)
                                }

                                val timeString = "${message.createdAt.hour}:${String.format("%02d", message.createdAt.minute)}"

                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        if ((message.isOwnMessage && dismissValue == SwipeToDismissBoxValue.EndToStart) ||
                                            (!message.isOwnMessage && dismissValue == SwipeToDismissBoxValue.StartToEnd)) {
                                            onSetReplyMessage(message)
                                        }
                                        false
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = !message.isOwnMessage && !message.isRevoked,
                                    enableDismissFromEndToStart = message.isOwnMessage && !message.isRevoked,
                                    backgroundContent = {
                                        val isSwiping = (message.isOwnMessage && dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) || (!message.isOwnMessage && dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd)
                                        if (isSwiping) {
                                            Box(modifier = Modifier.fillMaxSize().background(PrimaryBlue.copy(alpha = 0.15f)).padding(horizontal = 16.dp), contentAlignment = if (message.isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart) {
                                                Icon(Icons.AutoMirrored.Rounded.Reply, contentDescription = "Trả lời", tint = PrimaryBlue)
                                            }
                                        }
                                    }
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                                        MessageBubble(
                                            message = message.content,
                                            senderName = message.sender.displayName ?: "Unknown",
                                            isOwnMessage = message.isOwnMessage,
                                            senderAvatarUrl = message.sender.avatarUrl,
                                            time = timeString,
                                            type = message.type,
                                            fileUrl = message.fileUrl,
                                            fileName = message.fileName,
                                            fileSize = message.fileSize,
                                            replyTo = message.replyTo,
                                            isRevoked = message.isRevoked,
                                            isSenderOnline = message.sender.isOnline,
                                            onImageClick = { selectedImageToView = it },
                                            onPinClick = { onTogglePinMessage(message.uuid.toString()) },
                                            onRevokeClick = { onRevokeMessage(message.uuid.toString()) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !showSharedMedia,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200))
            ) {
                ChatComposer(
                    showSuggestions = showMentionSuggestions,
                    suggestions = mentionSuggestions,
                    replyingTo = replyingTo,
                    onSelectUserMention = onSelectUserMention,
                    onChatInputChanged = onChatInputChanged,
                    onSetReplyMessage = onSetReplyMessage,
                    onSend = { content, mediaUri, mediaType ->
                        val mediaBytes = mediaUri?.let { uri ->
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        }
                        val fileName = mediaUri?.let { getFileNameFromUri(context, it) }
                        onSendMessage(content, mediaBytes, mediaType, fileName)
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatComposer(
    showSuggestions: Boolean,
    suggestions: List<User>,
    replyingTo: com.example.se405.android.features.chat_management.domain.entity.MessageEntity?,
    onSelectUserMention: (User) -> Unit,
    onChatInputChanged: (String) -> Unit,
    onSetReplyMessage: (com.example.se405.android.features.chat_management.domain.entity.MessageEntity?) -> Unit,
    onSend: (String, Uri?, String?) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        var inputTextField by remember { mutableStateOf(TextFieldValue("")) }

        AnimatedVisibility(
            visible = showSuggestions && suggestions.isNotEmpty(),
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                shadowElevation = 12.dp,
                color = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(suggestions) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val lastAtIndex = inputTextField.text.lastIndexOf('@')
                                    if (lastAtIndex != -1) {
                                        val prefix = inputTextField.text.substring(0, lastAtIndex)
                                        val newText = "$prefix@${user.displayName}​ "
                                        inputTextField = TextFieldValue(text = newText, selection = TextRange(newText.length))
                                    }
                                    onSelectUserMention(user)
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(
                                    if (user.displayName == "Mọi người") Color(0xFFFF7043) else PrimaryBlue.copy(alpha = 0.15f)
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (user.displayName == "Mọi người") {
                                    Icon(Icons.Rounded.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                } else if (!user.avatarUrl.isNullOrBlank()) {
                                    GlideImage(model = user.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else if (user.displayName != null) {
                                    Text(user.displayName.take(1).uppercase(), color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(text = user.displayName ?: "Unknown", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color.Black)
                        }
                    }
                }
            }
        }

        ChatInputBar(
            textValue = inputTextField,
            replyingTo = replyingTo,
            onTextChanged = { newTextValue ->
                inputTextField = newTextValue
                onChatInputChanged(newTextValue.text)
            },
            onCancelReply = { onSetReplyMessage(null) },
            onSendMessage = { content, mediaUri, mediaType ->
                onSend(content, mediaUri, mediaType)
                inputTextField = TextFieldValue("")
            }
        )
    }
}

@Composable
private fun ChatSearchResults(
    searchKeyword: String,
    searchResults: List<com.example.se405.android.features.chat_management.domain.entity.MessageEntity>,
    onResultClick: (com.example.se405.android.features.chat_management.domain.entity.MessageEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (searchKeyword.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nhập từ khóa để tìm kiếm", color = Color.Gray) }
        } else if (searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Không tìm thấy kết quả nào", color = Color.Gray) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(searchResults) { msg ->
                    val dateStr = try { msg.createdAt.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) } catch (e: Exception) {
                        Log.e("ChatRoom", "Error formatting date: ${e.message}")
                        ""
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .clickable { onResultClick(msg) }
                            .padding(12.dp)
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            if (!msg.sender.avatarUrl.isNullOrBlank()) {
                                GlideImage(model = msg.sender.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else if (msg.sender.displayName != null) {
                                Text(msg.sender.displayName.take(1).uppercase(), color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(msg.sender.displayName ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(dateStr, fontSize = 12.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(msg.content, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
