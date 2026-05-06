package com.example.se405.android.features.chat_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun ChatTopBar(
    taskName: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Avatar giả lập của Task/Group
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)) // Xanh dương nhạt
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = taskName,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        // Các icon chức năng
        IconButton(onClick = { /* TODO: Audio Call */ }) {
            Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = Color.Black)
        }
        IconButton(onClick = { /* TODO: Video Call */ }) {
            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.Black)
        }
    }
}

// Đặt ở cuối file ChatTopBar.kt
@Preview(showBackground = true)
@Composable
fun ChatTopBarPreview() {
    ChatTopBar(
        taskName = "Thiết kế Database",
        onBackClick = {}
    )
}