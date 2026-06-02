package com.example.se405.android.features.chat_management.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.se405.android.features.chat_management.domain.entity.MessageEntity

@Composable
fun PinnedMessagesBanner(pinnedMessages: List<MessageEntity>, onMessageClick: (MessageEntity) -> Unit) {
    if (pinnedMessages.isEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }
    val latestPinned = pinnedMessages.last()

    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.PushPin, contentDescription = "Ghim", tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f).clickable { onMessageClick(latestPinned) }) {
                Text("Tin nhắn đã ghim", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (latestPinned.type == "FILE") "[Tài liệu] ${latestPinned.fileName}" else if (latestPinned.type == "IMAGE") "[Hình ảnh]" else latestPinned.content,
                    fontSize = 14.sp, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }

            if (pinnedMessages.size > 1) {
                IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown, contentDescription = "Mở rộng", tint = Color.Gray)
                }
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(bottom = 8.dp))
                val olderPinned = pinnedMessages.dropLast(1).takeLast(2)

                olderPinned.forEach { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onMessageClick(msg) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(32.dp))
                        Text(
                            text = if (msg.type == "FILE") "[Tài liệu] ${msg.fileName}" else if (msg.type == "IMAGE") "[Hình ảnh]" else msg.content,
                            fontSize = 14.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}