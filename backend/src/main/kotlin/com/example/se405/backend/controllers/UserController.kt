package com.example.se405.backend.controllers

import com.example.se405.backend.database.repository.UserRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Controller
class UserController(
    private val userRepository: UserRepository
) {

    @MutationMapping
    fun updateFcmToken(@Argument token: String): Boolean {
        val auth = SecurityContextHolder.getContext().authentication

        if (auth == null || auth.name == "anonymousUser") {
            println("❌ Lỗi: Chưa đăng nhập, không thể lưu Token!")
            return false
        }

        val user = userRepository.findByUsername(auth.name)
        if (user == null) {
            println("❌ Lỗi: Không tìm thấy User trong Database!")
            return false
        }

        user.fcmToken = token
        userRepository.save(user)

        println("📥 Đã cập nhật FCM Token mới cho user: ${user.username}")
        return true
    }
}