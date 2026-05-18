@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.se405.android.features.chat_management.presentation.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Dummy data cho User (Chờ API lấy danh sách User từ Backend)
data class UserContact(val id: String, val name: String, val role: String)
val dummyUsers = listOf(
    UserContact("u1", "Nguyễn Văn An", "Project Manager"),
    UserContact("u2", "Trần Thị Bình", "UI/UX Designer"),
    UserContact("u3", "Lê Hoàng Minh", "Backend Developer"),
    UserContact("u4", "Phạm Trà My", "Marketing")
)

@Composable
fun NewMessageScreen(
    onClose: () -> Unit,
    onNext: (selectedUserIds: List<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedUsers by remember { mutableStateOf(setOf<String>()) }

    // Sử dụng màu xanh dương đậm (giống nút Add Task của bạn)
    // để tránh bị chìm vào nền do lỗi Theme
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
                        onClick = { onNext(selectedUsers.toList()) },
                        enabled = selectedUsers.isNotEmpty()
                    ) {
                        Text(
                            text = "Next",
                            fontWeight = FontWeight.Bold,
                            // Chữ Next sáng màu xanh khi có người được chọn
                            color = if (selectedUsers.isNotEmpty()) activeColor else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Thanh tìm kiếm "To: "
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search name or group", color = Color.Gray) },
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

            // Danh sách người dùng
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                val filteredUsers = dummyUsers.filter { it.name.contains(searchQuery, ignoreCase = true) }

                items(filteredUsers) { user ->
                    val isSelected = selectedUsers.contains(user.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedUsers = if (isSelected) {
                                    selectedUsers - user.id
                                } else {
                                    selectedUsers + user.id
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Tên & Role
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = user.role, color = Color.Gray, fontSize = 13.sp)
                        }

                        // Icon Checkmark (Thay cho RadioButton bị lỗi màu)
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Đã chọn",
                                tint = activeColor, // Màu xanh nổi bật
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