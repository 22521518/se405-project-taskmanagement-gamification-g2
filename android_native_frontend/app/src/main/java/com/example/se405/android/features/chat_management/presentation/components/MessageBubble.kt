package com.example.se405.android.features.chat_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageBubble(
    message: String,
    senderName: String,
    isOwnMessage: Boolean,
    time: String
) {
    val backgroundColor = if (isOwnMessage) Color(0xFFE3F2FD) else Color.White
    val alignment = if (isOwnMessage) Alignment.End else Alignment.Start
    val shape = if (isOwnMessage) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        if (!isOwnMessage) {
            Text(
                text = senderName,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .clip(shape)
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            // Note: Để làm Highlight @mention màu vàng, ta sẽ dùng AnnotatedString ở bước sau.
            // Tạm thời hiển thị text cơ bản.
            Text(text = message, color = Color.Black)
        }

        Text(
            text = time,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp, end = 4.dp)
        )
    }
}

// Đặt ở cuối file MessageBubble.kt
@Preview(showBackground = true)
@Composable
fun MessageBubblePreview() {
    Column {
        // Preview tin nhắn của người khác
        MessageBubble(
            message = "Chào bạn, bug này sửa xong chưa?",
            senderName = "Nguyễn Văn A",
            isOwnMessage = false,
            time = "14:00"
        )
        // Preview tin nhắn của mình
        MessageBubble(
            message = "Mình đang kiểm tra lại lần cuối nhé!",
            senderName = "Tôi",
            isOwnMessage = true,
            time = "14:05"
        )
    }
}