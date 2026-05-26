package com.example.se405.android.features.chat_management.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class, ExperimentalAnimationApi::class)
@Composable
fun ChatInputBar(
    onSendMessage: (content: String, mediaUri: Uri?, mediaType: String?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }

    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMediaType by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                selectedMediaUri = it
                selectedMediaType = "IMAGE"
            }
            showBottomSheet = false
        }
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> /* Xử lý file sau */ showBottomSheet = false }
    )

    val activeColor = Color(0xFF0084FF) // Màu xanh chuẩn Messenger
    val surfaceColor = Color(0xFFFFFFFF)
    val inputBackgroundColor = Color(0xFFF0F2F5) // Màu nền xám nhạt cực êm

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = surfaceColor,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BottomSheetActionItem(
                        icon = Icons.Rounded.Image,
                        title = "Hình ảnh",
                        containerColor = Color(0xFFE7F3FF),
                        iconTint = activeColor,
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                    BottomSheetActionItem(
                        icon = Icons.Rounded.AttachFile,
                        title = "Tài liệu",
                        containerColor = Color(0xFFF0F2F5),
                        iconTint = Color.DarkGray,
                        onClick = { filePickerLauncher.launch("*/*") }
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceColor)
            .navigationBarsPadding()
    ) {
        // Vùng Preview Ảnh cực mượt
        AnimatedVisibility(
            visible = selectedMediaUri != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(modifier = Modifier.padding(start = 52.dp, top = 8.dp, end = 16.dp, bottom = 4.dp)) {
                if (selectedMediaType == "IMAGE") {
                    GlideImage(
                        model = selectedMediaUri,
                        contentDescription = "Selected photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                IconButton(
                    onClick = {
                        selectedMediaUri = null
                        selectedMediaType = null
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = 8.dp, y = (-8).dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Xóa", tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Thanh nhập liệu (Kiểu dáng Messenger)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom // Căn đáy để khi gõ text dài không bị xô lệch icon
        ) {
            // Nút (+) Đính kèm
            IconButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Attach", tint = activeColor, modifier = Modifier.size(28.dp))
            }

            // Ô nhập Text bo tròn 100%
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(inputBackgroundColor)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text("Nhập tin nhắn...", color = Color.Gray, fontSize = 15.sp)
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                    cursorBrush = SolidColor(activeColor),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Nút Send
            val isEnabled = text.isNotBlank() || selectedMediaUri != null
            IconButton(
                onClick = {
                    if (isEnabled) {
                        onSendMessage(text.trim(), selectedMediaUri, selectedMediaType)
                        text = ""
                        selectedMediaUri = null
                        selectedMediaType = null
                    }
                },
                enabled = isEnabled,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    tint = if (isEnabled) activeColor else Color.LightGray,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun RowScope.BottomSheetActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(28.dp), tint = iconTint)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
    }
}