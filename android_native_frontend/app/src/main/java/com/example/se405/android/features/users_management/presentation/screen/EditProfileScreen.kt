package com.example.se405.android.features.users_management.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.se405.android.features.users_management.presentation.viewmodel.EditProfileViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun EditProfileScreen(
    currentName: String,
    currentEmail: String,
    currentAvatarUrl: String?,
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Khởi tạo trình chọn ảnh (Photo Picker) của Android
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // User chọn ảnh xong -> nhét vào ViewModel
        if (uri != null) {
            viewModel.selectedImageUri = uri
        }
    }

    // Khởi tạo dữ liệu cũ vào form
    LaunchedEffect(Unit) {
        viewModel.initData(currentName, currentEmail, currentAvatarUrl)
    }

    val activeColor = Color(0xFF2563EB)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp,
                            color = activeColor
                        )
                    } else {
                        // Truyền context vào hàm update để phục vụ cho việc nén ảnh
                        TextButton(onClick = { viewModel.update(context, onBack) }) {
                            Text("Save", color = activeColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = Color.Unspecified
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Avatar Editor
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        // Nền avatar dịu mắt hơn
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.selectedImageUri != null) {
                        // Dùng GlideImage hiển thị ảnh vừa chọn từ máy
                        GlideImage(
                            model = viewModel.selectedImageUri,
                            contentDescription = "New Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!viewModel.avatarUrl.isNullOrBlank()) {
                        // Dùng GlideImage hiển thị ảnh đang có từ trên mạng (nếu có)
                        GlideImage(
                            model = viewModel.avatarUrl,
                            contentDescription = "Current Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Hiển thị chữ cái đầu tiên nếu không có ảnh
                        Text(
                            text = currentName.take(1).uppercase(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor.copy(alpha = 0.8f) // Chữ đồng bộ với màu accent
                        )
                    }
                }

                // Nút thay đổi ảnh (nổi bật với viền trắng tạo hiệu ứng khoét)
                Surface(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .offset(x = 4.dp, y = 4.dp), // Kéo dịch ra ngoài một chút cho đẹp
                    shape = CircleShape,
                    color = activeColor,
                    border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.background),
                    shadowElevation = 2.dp
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Change Avatar",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Input Fields
            EditField(
                label = "Full Name",
                value = viewModel.displayName,
                icon = Icons.Rounded.Person,
                onValueChange = { viewModel.displayName = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            EditField(
                label = "Email Address",
                value = viewModel.email,
                icon = Icons.Rounded.Email,
                onValueChange = { viewModel.email = it }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Username and user ID cannot be changed.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.8f)
            )

            // Hiển thị lỗi nếu có
            if (viewModel.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = viewModel.error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EditField(
    label: String,
    value: String,
    icon: ImageVector,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge, // Bo góc cực lớn chuẩn hiện đại
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                // Ẩn viền đi khi không focus
                unfocusedBorderColor = Color.Transparent,
                // Viền sáng lên mờ mờ khi đang gõ
                focusedBorderColor = Color(0xFF2563EB).copy(alpha = 0.5f),
                // Nền xám nhạt thay cho viền
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                cursorColor = Color(0xFF2563EB)
            ),
            singleLine = true
        )
    }
}