package com.example.se405.backend.configs

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import jakarta.annotation.PostConstruct

@Configuration
class FirebaseConfig {

    @PostConstruct
    fun initialize() {
        try {
            // Đọc file json từ thư mục resources
            val serviceAccount = ClassPathResource("firebase-service-account.json").inputStream

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            // Tránh việc Firebase bị khởi tạo lại nhiều lần gây lỗi
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                println("🚀🚀🚀 Firebase Admin đã kết nối THÀNH CÔNG! Sẵn sàng bắn Notification!")
            }
        } catch (e: Exception) {
            println("🚨 Lỗi khởi tạo Firebase Admin: ${e.message}")
            e.printStackTrace()
        }
    }
}