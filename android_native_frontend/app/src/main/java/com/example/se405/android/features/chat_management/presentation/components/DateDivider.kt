package com.example.se405.android.features.chat_management.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 1. Tạo một Composable hiển thị thanh ngày tháng tối giản, hiện đại
@Composable
fun DateDivider(date: LocalDate, modifier: Modifier = Modifier) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    val text = when (date) {
        today -> "Hôm nay"
        yesterday -> "Hôm qua"
        else -> date.format(formatter)
    }

    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFFE2E8F0), // Màu xám nhạt tinh tế
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = text,
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}