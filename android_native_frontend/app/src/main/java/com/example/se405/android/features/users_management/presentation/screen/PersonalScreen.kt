package com.example.se405.android.features.users_management.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.se405.android.core.presentation.components.AppHeader
import com.example.se405.android.features.users_management.presentation.components.SettingRowClickable
import com.example.se405.android.features.users_management.presentation.components.SettingRowSwitch
import com.example.se405.android.features.users_management.presentation.viewmodel.PersonalViewModel
import org.koin.androidx.compose.koinViewModel

import com.example.se405.android.core.authentication.data.UserProfileResponse

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun PersonalScreen(
    onLogoutClick: () -> Unit,
    // Cập nhật signature để chuyền avatarUrl
    onEditClick: (displayName: String, email: String, avatarUrl: String?) -> Unit,
    viewModel: PersonalViewModel = koinViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // TỰ ĐỘNG CẬP NHẬT: Lắng nghe sự kiện vòng đời của màn hình
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Mỗi khi màn hình này nổi lên trên cùng (Sau khi từ Edit quay về)
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadUserProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PersonalScreenContent(
        userProfile = userProfile,
        isLoading = isLoading,
        error = error,
        onLogoutClick = { viewModel.logout(onSuccess = onLogoutClick) },
        onEditClick = onEditClick,
        onRetryClick = { viewModel.loadUserProfile() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun PersonalScreenContent(
    userProfile: UserProfileResponse?,
    isLoading: Boolean,
    error: String?,
    onLogoutClick: () -> Unit,
    onEditClick: (displayName: String, email: String, avatarUrl: String?) -> Unit,
    onRetryClick: () -> Unit
) {
    var isOnline by remember { mutableStateOf(true) }
    val activeColor = Color(0xFF2563EB)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppHeader(title = "Profile")
        }
    ) { paddingValues ->
        when {
            isLoading && userProfile == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = activeColor)
                }
            }

            error != null && userProfile == null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.Notifications, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = error ?: "", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetryClick) { Text("Thử lại") }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // KHỐI HIỂN THỊ AVATAR
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile?.avatarUrl.isNullOrBlank()) {
                            // Hiển thị ảnh thật từ Cloudinary
                            GlideImage(
                                model = userProfile?.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Hiển thị chữ cái đầu nếu không có ảnh
                            Text(
                                text = (userProfile?.displayName?.take(1) ?: "U").uppercase(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeColor.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userProfile?.displayName ?: "Không có tên",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userProfile?.email ?: "Không có email",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(vertical = 8.dp)
                    ) {
                        SettingRowClickable(
                            icon = Icons.Rounded.Edit,
                            title = "Edit Personal Info",
                            iconTint = activeColor,
                            onClick = {
                                // Truyền đủ 3 biến sang màn hình Edit
                                onEditClick(
                                    userProfile?.displayName ?: "",
                                    userProfile?.email ?: "",
                                    userProfile?.avatarUrl
                                )
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingRowSwitch(
                            icon = Icons.Rounded.Notifications,
                            title = "Active Status",
                            subtitle = if (isOnline) "Online" else "Offline",
                            iconTint = if (isOnline) Color(0xFF10B981) else Color.Gray,
                            isChecked = isOnline,
                            onCheckedChange = { isOnline = it }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PersonalScreenPreview() {
    com.example.se405.android.core.presentation.theme.Android_Theme {
        PersonalScreenContent(
            userProfile = UserProfileResponse(
                uuid = "user1",
                email = "test@example.com",
                username = "testuser",
                displayName = "Test User",
                avatarUrl = null
            ),
            isLoading = false,
            error = null,
            onLogoutClick = {},
            onEditClick = { _, _, _ -> },
            onRetryClick = {}
        )
    }
}


