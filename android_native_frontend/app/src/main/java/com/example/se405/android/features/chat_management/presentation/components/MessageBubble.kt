package com.example.se405.android.features.chat_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.se405.android.features.chat_management.domain.entity.ReplyMessageInfo

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MessageBubble(
    message: String,
    senderName: String,
    isOwnMessage: Boolean,
    senderAvatarUrl: String?,
    time: String,
    type: String = "TEXT",
    fileUrl: String? = null,
    fileName: String? = null,
    fileSize: String? = null,
    replyTo: ReplyMessageInfo? = null,
    onImageClick: (String) -> Unit = {}
) {
    val bubbleColor = if (isOwnMessage) Color(0xFF2563EB) else Color(0xFFF3F4F6)
    val textColor = if (isOwnMessage) Color.White else Color.Black
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // 1. Vẽ Avatar (Nếu là tin nhắn người khác)
        if (!isOwnMessage) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (!senderAvatarUrl.isNullOrBlank() && senderAvatarUrl != "null") {
                    GlideImage(
                        model = senderAvatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = senderName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // 2. Cột chứa toàn bộ nội dung tin nhắn
        Column(
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
        ) {
            if (!isOwnMessage) {
                Text(
                    text = senderName,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            Column(
                horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp) // Khoảng cách giữa Ảnh và Text đi kèm
            ) {
                // --- A. KHỐI TRẢ LỜI ---
                if (replyTo != null) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.1f))
                            .padding(start = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(if (isOwnMessage) Color(0xFF3B82F6).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f))
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

                // --- B. KHỐI ẢNH HOẶC FILE ---
                if (type == "IMAGE" && !fileUrl.isNullOrBlank()) {
                    GlideImage(
                        model = fileUrl,
                        contentDescription = "Hình ảnh",
                        modifier = Modifier
                            .width(240.dp)
                            .heightIn(max = 300.dp)
                            .clip(RoundedCornerShape(16.dp)) // Chỉ bo tròn, không có nền bong bóng
                            .clickable { onImageClick(fileUrl) },
                        contentScale = ContentScale.Crop
                    )
                } else if (type == "FILE") {
                    // Box nền cho File
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isOwnMessage) bubbleColor.copy(alpha = 0.9f) else bubbleColor)
                            .clickable {
                                fileUrl?.let { url ->
                                    try {
                                        val request = DownloadManager.Request(Uri.parse(url))
                                            .setTitle(fileName ?: "Tài liệu đính kèm")
                                            .setDescription("Đang tải file xuống máy...")
                                            // Hiển thị tiến trình trên thanh thông báo, cho phép bấm vào để mở khi tải xong
                                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                            .setDestinationInExternalPublicDir(
                                                Environment.DIRECTORY_DOWNLOADS,
                                                fileName ?: "document_${System.currentTimeMillis()}"
                                            )

                                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                        downloadManager.enqueue(request)
                                        Toast.makeText(context, "Đang tải file...", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Không thể tải file!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val extension = fileName?.substringAfterLast('.', "").orEmpty().lowercase()
                            val iconFile = when (extension) {
                                "pdf" -> Icons.Rounded.PictureAsPdf
                                "doc", "docx" -> Icons.Rounded.Description
                                "xls", "xlsx" -> Icons.Rounded.TableChart
                                "zip", "rar" -> Icons.Rounded.FolderZip
                                else -> Icons.Rounded.InsertDriveFile
                            }
                            val iconColor = if (extension == "pdf") Color(0xFFEF4444) else Color(0xFF3B82F6)

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(iconFile, contentDescription = "File Type", tint = iconColor)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fileName ?: "Tài liệu",
                                    color = textColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = fileSize ?: "0 KB",
                                    color = if (isOwnMessage) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(Icons.Rounded.Download, contentDescription = "Download", tint = textColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // --- C. KHỐI TEXT (Có bong bóng đàng hoàng) ---
                if (message.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                                    bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                                )
                            )
                            .background(bubbleColor)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = message,
                            color = textColor,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // 3. Thời gian gửi
            Text(
                text = time,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp, start = 4.dp)
            )
        }
    }
}