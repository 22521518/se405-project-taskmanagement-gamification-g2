package com.example.se405.android.core.utils

fun formatTimeAgo(dateTime: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val duration = java.time.Duration.between(dateTime, now)
    return when {
        duration.toMinutes() < 1 -> "Vừa xong"
        duration.toHours() < 1 -> "${duration.toMinutes()}p"
        duration.toDays() < 1 -> "${duration.toHours()} giờ"
        duration.toDays() < 7 -> "${duration.toDays()} ngày"
        else -> "${dateTime.dayOfMonth}/${dateTime.monthValue}"
    }
}