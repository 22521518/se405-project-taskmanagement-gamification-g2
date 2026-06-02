package com.example.se405.android.features.chat_management.presentation.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SharedMediaView(
    images: List<MessageEntity>,
    files: List<MessageEntity>,
    links: List<MessageEntity>,
    onImageClick: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ảnh", "Tài liệu", "Liên kết")

    val primaryBlue = Color(0xFF2563EB)
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current // Dùng để mở link web

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Kho lưu trữ",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = primaryBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = primaryBlue
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedTabIndex == index) primaryBlue else Color.Gray
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTabIndex) {
                // ==========================
                // TAB 0: HÌNH ẢNH
                // ==========================
                0 -> {
                    if (images.isEmpty()) {
                        EmptyStateText("Chưa có hình ảnh nào")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(images) { msg ->
                                if (!msg.fileUrl.isNullOrBlank()) {
                                    GlideImage(
                                        model = msg.fileUrl,
                                        contentDescription = "Shared Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onImageClick(msg.fileUrl) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================
                // TAB 1: TÀI LIỆU
                // ==========================
                1 -> {
                    if (files.isEmpty()) {
                        EmptyStateText("Chưa có tài liệu nào")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(files) { msg ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // TODO: Thêm logic tải file hoặc mở file bằng Intent
                                            Toast.makeText(context, "Đang tải file: ${msg.fileName}", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(primaryBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.InsertDriveFile, contentDescription = "File", tint = primaryBlue)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = msg.fileName ?: "Tài liệu không tên",
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${msg.fileSize ?: "0 KB"} • Gửi bởi ${msg.sender.displayName}",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================
                // TAB 2: LIÊN KẾT (LINKS)
                // ==========================
                2 -> {
                    if (links.isEmpty()) {
                        EmptyStateText("Chưa có liên kết nào")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(links) { msg ->
                                val dateStr = try {
                                    msg.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                } catch (e: Exception) { "" }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                // Đảm bảo link có chứa http/https trước khi mở để tránh crash
                                                val url = if (!msg.content.startsWith("http")) "https://${msg.content}" else msg.content
                                                uriHandler.openUri(url)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Không thể mở liên kết này", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Link, contentDescription = "Link", tint = Color(0xFF10B981))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = msg.content,
                                            fontWeight = FontWeight.Medium,
                                            color = primaryBlue,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${msg.sender.displayName} • $dateStr",
                                            fontSize = 12.sp,
                                            color = Color.Gray
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

@Composable
private fun BoxScope.EmptyStateText(text: String) {
    Text(
        text = text,
        modifier = Modifier.align(Alignment.Center),
        color = Color.Gray,
        fontWeight = FontWeight.Medium
    )
}