package com.example.se405.android.features.chat_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import com.example.se405.android.features.chat_management.domain.entity.ReplyMessageInfo

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MessageBubble(
    message: String,
    senderName: String,
    senderAvatarUrl: String?,
    isOwnMessage: Boolean,
    time: String,
    replyTo: ReplyMessageInfo? = null,
    onImageClick: (String) -> Unit = {}
) {
    // --- 1. LOGIC BÓC TÁCH LINK ẢNH ---
    // Tìm kiếm chuỗi [IMAGE:...] trong tin nhắn
    val imageUrlPrefix = "[IMAGE:"
    var textContent = message
    var imageUrl: String? = null

    if (message.contains(imageUrlPrefix) && message.endsWith("]")) {
        val startIndex = message.indexOf(imageUrlPrefix)
        val endIndex = message.lastIndexOf("]")
        if (startIndex != -1 && endIndex > startIndex) {
            // Lấy URL ảnh
            imageUrl = message.substring(startIndex + imageUrlPrefix.length, endIndex)
            // Lấy phần text (nếu có gõ chữ kèm ảnh)
            textContent = message.substring(0, startIndex).trim()
        }
    }

    // --- 2. CẤU HÌNH GIAO DIỆN (ZALO / MESSENGER STYLE) ---
    val bubbleColor = if (isOwnMessage) Color(0xFF0084FF) else Color(0xFFF1F0F0) // Xanh dương cho mình, Xám nhạt cho người khác
    val textColor = if (isOwnMessage) Color.White else Color.Black
    val bubbleShape = if (isOwnMessage) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // --- ẢNH ĐẠI DIỆN ĐỐI PHƯƠNG ---
        if (!isOwnMessage) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!senderAvatarUrl.isNullOrBlank()) {
                    GlideImage(
                        model = senderAvatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = senderName.take(1).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // --- KHỐI BONG BÓNG + TÊN + THỜI GIAN ---
        Column(
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
        ) {
            // Tên người gửi (Hiện trên đầu bong bóng của người khác)
            if (!isOwnMessage) {
                Text(
                    text = senderName,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            // Khối bong bóng chứa Nội dung
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleColor)
                    .padding(if (imageUrl != null && textContent.isEmpty()) 2.dp else 12.dp)
            ) {
                Column {
                    // Nếu có ảnh, hiển thị ảnh trước
                    if (imageUrl != null) {
                        GlideImage(
                            model = imageUrl,
                            contentDescription = "Hình ảnh đính kèm",
                            modifier = Modifier
                                .widthIn(max = 220.dp) // Giới hạn chiều rộng ảnh
                                .heightIn(max = 300.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageClick(imageUrl) },
                            contentScale = ContentScale.Crop
                        )
                        if (textContent.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    if (replyTo != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.1f))
                                .padding(start = 4.dp) // Dành không gian cho viền trái
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(if (isOwnMessage) Color(0xFF3B82F6) else Color.White)
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = replyTo.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isOwnMessage) Color.White else Color(0xFF2563EB)
                                )
                                Text(
                                    text = replyTo.content.replace("\n", " "),
                                    fontSize = 12.sp,
                                    color = if (isOwnMessage) Color.White.copy(alpha = 0.8f) else Color.DarkGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Hiển thị Text (Nếu có)
                    if (textContent.isNotEmpty()) {
                        Text(
                            text = textContent,
                            color = textColor,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Thời gian gửi
            Text(
                text = time,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp, start = 4.dp)
            )
        }
    }
}