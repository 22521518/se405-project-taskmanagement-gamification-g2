@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.se405.android.core.authentication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.se405.android.core.authentication.BiometricViewmodel
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.theme.AppText
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthSettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onGoHome: () -> Unit,
    viewModel: BiometricViewmodel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Cài đặt tài khoản",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
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

            // 1. Khối Thông tin Người dùng (Centered Profile)
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (viewModel.displayName ?: "U").take(1).uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = viewModel.displayName ?: "User",
                style = AppText.HeadBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "@${viewModel.username ?: "unknown"}",
                style = AppText.BodyRegular,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 2. Khối Cài đặt Bảo mật
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Bảo mật",
                    style = AppText.BodyBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Biometric Toggle Item phẳng (Không dùng Card bọc ngoài)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.large
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Đăng nhập sinh trắc học",
                            style = AppText.BodyBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Dùng vân tay hoặc khuôn mặt để mở khóa",
                            style = AppText.CaptionRegular,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Switch(
                        checked = viewModel.isBiometricEnabled,
                        onCheckedChange = {
                            activity?.let { act ->
                                viewModel.toggleBiometric(act, it)
                            }
                        }
                    )
                }

                if (viewModel.error != null) {
                    Text(
                        text = viewModel.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = AppText.CaptionRegular,
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. Nút Đăng xuất
            ButtonApp(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                type = ButtonType.OUTLINED,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Đăng xuất",
                    style = AppText.BodyBold,
                    color = MaterialTheme.colorScheme.error // Dùng màu cảnh báo cho nút thoát
                )
            }
        }
<<<<<<< HEAD
=======

        if (viewModel.error != null) {
            Text(
                text = viewModel.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = AppText.CaptionRegular,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        ButtonApp(
            onClick = onGoHome,
            type = ButtonType.FILLED,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Home", style = AppText.BodyBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        ButtonApp(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            type = ButtonType.OUTLINED,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Logout", style = AppText.BodyBold)
        }

        Spacer(modifier = Modifier.height(32.dp))
>>>>>>> origin/dev
    }
}