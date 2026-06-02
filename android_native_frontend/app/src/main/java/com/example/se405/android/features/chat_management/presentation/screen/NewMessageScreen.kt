@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.example.se405.android.features.chat_management.presentation.screen

import androidx.compose.foundation.background
import java.time.LocalDateTime
import kotlin.uuid.Uuid
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.koin.androidx.compose.koinViewModel
import com.example.se405.android.features.chat_management.presentation.viewmodel.NewMessageViewModel
import kotlin.uuid.ExperimentalUuidApi

import com.example.se405.android.features.users_management.domain.entity.User

@OptIn(ExperimentalUuidApi::class)
@Composable
fun NewMessageScreen(
    onClose: () -> Unit,
    onNext: (selectedUserIds: List<String>, chatName: String) -> Unit,
    viewModel: NewMessageViewModel = koinViewModel()
) {
    val usersList by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    NewMessageScreenContent(
        usersList = usersList,
        isLoading = isLoading,
        error = error,
        onClose = onClose,
        onNext = onNext
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun NewMessageScreenContent(
    usersList: List<User>,
    isLoading: Boolean,
    error: String?,
    onClose: () -> Unit,
    onNext: (selectedUserIds: List<String>, chatName: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedUsers by remember { mutableStateOf(setOf<String>()) }

    val activeColor = Color(0xFF2563EB)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Message", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Đóng")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val idsList = selectedUsers.toList()
                            val chatName = if (idsList.size > 1) {
                                "Nhóm chat mới"
                            } else {
                                val selectedUser = usersList.find { it.uuid.toString() == idsList.first() }
                                selectedUser?.displayName ?: "Chat 1:1"
                            }
                            onNext(idsList, chatName)
                        },
                        enabled = selectedUsers.isNotEmpty()
                    ) {
                        Text(
                            text = "Next",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedUsers.isNotEmpty()) activeColor else Color.Gray
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search name or email", color = Color.Gray) },
                leadingIcon = { Text("To: ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp)) },
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = activeColor.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = activeColor)
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Lỗi: $error", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        val filteredUsers = usersList.filter {
                            it.displayName != null && it.email != null && (it.displayName.contains(searchQuery, ignoreCase = true) ||
                                    it.email.contains(searchQuery, ignoreCase = true))
                        }

                        items(filteredUsers) { user ->
                            val userIdStr = user.uuid.toString()
                            val isSelected = selectedUsers.contains(userIdStr)
                            if (user.displayName != null && user.email != null)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedUsers = if (isSelected) {
                                                selectedUsers - userIdStr
                                            } else {
                                                selectedUsers + userIdStr
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // 💡 ĐÃ SỬA: Logic kiểm tra và load Avatar thật
                                        if (!user.avatarUrl.isNullOrBlank() && user.avatarUrl != "null") {
                                            GlideImage(
                                                model = user.avatarUrl,
                                                contentDescription = "Avatar",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = user.displayName.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = user.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(text = user.email, color = Color.Gray, fontSize = 13.sp)
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Đã chọn",
                                            tint = activeColor,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.RadioButtonUnchecked,
                                            contentDescription = "Chưa chọn",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(28.dp)
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

@OptIn(ExperimentalUuidApi::class)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NewMessageScreenPreview() {
    com.example.se405.android.core.presentation.theme.Android_Theme {
        NewMessageScreenContent(
            usersList = listOf(
                User(uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"), email = "alice@example.com", username = "alice", displayName = "Alice", avatarUrl = null, isOnline = true, createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()),
                User(uuid = Uuid.parse("00000000-0000-0000-0000-000000000002"), email = "bob@example.com", username = "bob", displayName = "Bob", avatarUrl = null, isOnline = false, createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now())
            ),
            isLoading = false,
            error = null,
            onClose = {},
            onNext = { _, _ -> }
        )
    }
}