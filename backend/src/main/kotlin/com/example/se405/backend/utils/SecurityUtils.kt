package com.example.se405.backend.utils

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

/**
 * Hàm tiện ích top-level giúp trích xuất UUID của người dùng hiện tại từ SecurityContext.
 * Có thể gọi trực tiếp ở bất kỳ Controller hay Service nào trong Backend.
 */
fun getCurrentUserUuid(): UUID {
    val authentication = SecurityContextHolder.getContext().authentication
        ?: throw IllegalStateException("Người dùng chưa được xác thực trong hệ thống!")

    val principal = authentication.principal

    return when (principal) {
        // Trường hợp 1: Principal đã là UUID sẵn
        is UUID -> principal

        // Trường hợp 2: Principal là chuỗi String (Ví dụ chuỗi UUID từ JWT token)
        is String -> {
            try {
                UUID.fromString(principal)
            } catch (e: Exception) {
                throw IllegalStateException("Principal dạng String không đúng định dạng UUID: $principal")
            }
        }

        // Trường hợp 3: Principal là UserDetails mặc định của Spring Security
        is UserDetails -> {
            try {
                // Thường nếu bạn lưu UUID vào trường username khi tạo UserDetails
                UUID.fromString(principal.username)
            } catch (e: Exception) {
                throw IllegalStateException("Không thể chuyển đổi username sang UUID: ${principal.username}")
            }
        }

        // Trường hợp loại trừ bảo vệ hệ thống không bị crash ngầm
        else -> throw IllegalStateException("Không xác định được kiểu dữ liệu Principal: ${principal?.javaClass?.name}")
    }
}