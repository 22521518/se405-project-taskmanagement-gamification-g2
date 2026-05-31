@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.example.se405.android.features.chat_management.presentation.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import com.example.se405.android.features.chat_management.presentation.components.ChatInputBar
import com.example.se405.android.features.chat_management.presentation.components.ChatOptionItem
import com.example.se405.android.features.chat_management.presentation.components.MessageBubble
import com.example.se405.android.features.chat_management.presentation.components.PinnedMessagesBanner
import com.example.se405.android.features.chat_management.presentation.components.SharedMediaView
import com.example.se405.android.features.chat_management.presentation.viewmodel.TaskChatViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import com.example.se405.android.features.chat_management.presentation.components.DateDivider

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
    val sharedMedia by viewModel.sharedMediaState.collectAsState()
    val sharedLinks by viewModel.sharedLinksState.collectAsState()

    // State Tìm kiếm
    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResultsState.collectAsState()
    var searchKeyword by remember { mutableStateOf("") }

    val currentChatName by viewModel.chatNameState.collectAsState()
    val replyingTo by viewModel.replyingToMessage.collectAsState()
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

    val primaryBlue = Color(0xFF2563EB)

    LaunchedEffect(taskId, pendingParticipantIds) {
        viewModel.initChat(taskId, isFromTask, pendingParticipantIds, taskName)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // ==========================================
    // CÁC DIALOG VÀ BOTTOM SHEETS
    // ==========================================

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
        ModalBottomSheet(onDismissRequest = { showChatOptions = false }, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Tùy chọn đoạn chat", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                ChatOptionItem(icon = Icons.Rounded.Edit, title = "Đổi tên nhóm", baseColor = Color(0xFF8B5CF6)) {
                    showChatOptions = false
                    newChatNameInput = currentChatName.ifBlank { taskName }
                    showRenameDialog = true
                }
                ChatOptionItem(icon = Icons.Rounded.Search, title = "Tìm kiếm trong đoạn chat", baseColor = primaryBlue) {
                    showChatOptions = false
                    viewModel.toggleSearchMode(true)
                }
                ChatOptionItem(icon = Icons.Rounded.PhotoLibrary, title = "Ảnh, file và liên kết", baseColor = Color(0xFF10B981)) {
                    showChatOptions = false
                    showSharedMedia = true
                }
            }
        }
    }

    if (showSharedMedia) {
        LaunchedEffect(Unit) {
            viewModel.loadSharedMedia()
            viewModel.loadSharedLinks()
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

    // ==========================================
    // MÀN HÌNH CHÍNH (SCAFFOLD)
    // ==========================================
    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isSearching) {
                // 💡 GIAO DIỆN TÌM KIẾM MỚI (ĐẸP & GỌN GÀNG HƠN)
                Surface(
                    color = MaterialTheme.colorScheme.background, // Đồng nhất màu nền với toàn màn hình
                    shadowElevation = 2.dp, // Đổ bóng nhẹ mượt mà
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 4.dp, vertical = 8.dp), // Căn chỉnh lề cân đối
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nút Quay lại
                        IconButton(onClick = {
                            viewModel.toggleSearchMode(false)
                            searchKeyword = ""
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại", tint = primaryBlue)
                        }

                        // Khung nhập từ khóa
                        OutlinedTextField(
                            value = searchKeyword,
                            onValueChange = {
                                searchKeyword = it
                                viewModel.searchChat(it)
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
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(25.dp))
                        )
                    }
                }
            } else {
                // GIAO DIỆN THANH TIÊU ĐỀ BÌNH THƯỜNG
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    },
                    navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại") } },
                    actions = {
                        IconButton(onClick = { viewModel.toggleSearchMode(true) }) { Icon(Icons.Rounded.Search, contentDescription = "Tìm kiếm") }
                        IconButton(onClick = { showChatOptions = true }) { Icon(Icons.Rounded.Menu, contentDescription = "Tùy chọn") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = primaryBlue, navigationIconContentColor = primaryBlue, actionIconContentColor = primaryBlue)
                )
            }
        },
        bottomBar = {
            // 💡 ẨN THANH NHẬP CHAT KHI TÌM KIẾM HOẶC XEM KHO LƯU TRỮ
            AnimatedVisibility(
                visible = !isSearching && !showSharedMedia,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val showSuggestions by viewModel.showMentionSuggestions.collectAsState()
                    val suggestions by viewModel.mentionSuggestions.collectAsState()
                    var inputTextField by remember { mutableStateOf(TextFieldValue("")) }
                    // ==========================================
                    // POPUP GỢI Ý MENTION (ZALO STYLE)
                    // ==========================================
                    AnimatedVisibility(
                        visible = showSuggestions && suggestions.isNotEmpty(),
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
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
                                                    val newText = "$prefix@${user.displayName}\u200B "
                                                    inputTextField = TextFieldValue(
                                                        text = newText,
                                                        selection = TextRange(newText.length)
                                                    )
                                                }
                                                viewModel.selectUserMention(user)
                                            }
                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 💡 AVATAR HIỆN ĐẠI
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (user.displayName == "Mọi người") Color(0xFFFF7043)
                                                    else primaryBlue.copy(alpha = 0.15f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (user.displayName == "Mọi người") {
                                                Icon(Icons.Rounded.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            } else if (!user.avatarUrl.isNullOrBlank()) {
                                                GlideImage(model = user.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            } else {
                                                Text(user.displayName.take(1).uppercase(), color = primaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))

                                        // 💡 TÊN NGƯỜI DÙNG SẮC NÉT
                                        Text(
                                            text = user.displayName,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = Color.Black
                                        )
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
                            viewModel.onChatInputChanged(newTextValue.text)
                        },
                        onCancelReply = { viewModel.setReplyMessage(null) },
                        onSendMessage = { content, mediaUri, mediaType ->
                            val mediaBytes = mediaUri?.let { uri ->
                                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            }
                            val fileName = mediaUri?.let { getFileNameFromUri(context, it) }
                            viewModel.sendMessage(content, mediaBytes, mediaType, fileName)
                            inputTextField = TextFieldValue("")
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (isSearching) {
                // ==========================================
                // HIỂN THỊ KẾT QUẢ TÌM KIẾM
                // ==========================================
                Column(modifier = Modifier.fillMaxSize()) {
                    if (searchKeyword.isBlank()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nhập từ khóa để tìm kiếm", color = Color.Gray) }
                    } else if (searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Không tìm thấy kết quả nào", color = Color.Gray) }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(searchResults) { msg ->
                                val dateStr = try { msg.createdAt.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) } catch (e: Exception) { "" }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF8FAFC)) // Màu nền xám nhạt cho tin nhắn tìm được
                                        .clickable {
                                            viewModel.toggleSearchMode(false)
                                            searchKeyword = ""
                                            val targetIndex = uiState.messages.indexOfFirst { it.uuid == msg.uuid }
                                            if (targetIndex != -1) coroutineScope.launch { listState.animateScrollToItem(targetIndex) }
                                            else Toast.makeText(context, "Tin nhắn cũ chưa được tải", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(12.dp)
                                ) {
                                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(primaryBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        if (!msg.sender.avatarUrl.isNullOrBlank()) {
                                            GlideImage(model = msg.sender.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Text(msg.sender.displayName.take(1).uppercase(), color = primaryBlue, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(msg.sender.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primaryBlue)
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
            } else {
                // ==========================================
                // HIỂN THỊ KHUNG CHAT BÌNH THƯỜNG
                // ==========================================
                Column(modifier = Modifier.fillMaxSize()) {
                    PinnedMessagesBanner(pinnedMessages = uiState.pinnedMessages, onMessageClick = { pinnedMsg ->
                        val targetIndex = uiState.messages.indexOfFirst { it.uuid == pinnedMsg.uuid }
                        if (targetIndex != -1) coroutineScope.launch { listState.animateScrollToItem(targetIndex) }
                    })

                    if (uiState.pinnedMessages.isNotEmpty()) HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when {
                            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryBlue)
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
                                        // Lấy ngày của tin nhắn hiện tại
                                        val currentMessageDate = message.createdAt.toLocalDate()

                                        // Nếu là tin nhắn đầu tiên, hoặc ngày của tin nhắn này khác ngày của tin nhắn trước đó -> Hiện DateDivider
                                        if (index == 0 || uiState.messages[index - 1].createdAt.toLocalDate() != currentMessageDate) {
                                            DateDivider(date = currentMessageDate)
                                        }

                                        // Vẽ MessageBubble cũ của bạn bên dưới
                                        val timeString = "${message.createdAt.hour}:${String.format("%02d", message.createdAt.minute)}"

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
                                            enableDismissFromStartToEnd = !message.isOwnMessage && !message.isRevoked,
                                            enableDismissFromEndToStart = message.isOwnMessage && !message.isRevoked,
                                            backgroundContent = {
                                                val isSwiping = (message.isOwnMessage && dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) || (!message.isOwnMessage && dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd)
                                                if (isSwiping) {
                                                    Box(modifier = Modifier.fillMaxSize().background(primaryBlue.copy(alpha = 0.15f)).padding(horizontal = 16.dp), contentAlignment = if (message.isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart) {
                                                        Icon(Icons.AutoMirrored.Rounded.Reply, contentDescription = "Trả lời", tint = primaryBlue)
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
                                                    isRevoked = message.isRevoked,
                                                    onImageClick = { selectedImageToView = it },
                                                    onPinClick = { viewModel.togglePinMessage(message.uuid.toString()) },
                                                    onRevokeClick = { viewModel.revokeMessage(message.uuid.toString()) }
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
        }
    }
}