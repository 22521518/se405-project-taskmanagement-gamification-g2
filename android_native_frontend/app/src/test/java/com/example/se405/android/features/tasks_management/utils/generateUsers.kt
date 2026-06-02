package com.example.se405.android.features.tasks_management.utils

import com.example.se405.android.core.authentication.data.RegisterRequest
import java.lang.System.currentTimeMillis
import java.util.Locale.getDefault

fun generateUsers(count: Int): List<RegisterRequest> {
    val firstNames = listOf(
        "Nguyen", "Tran", "Le", "Pham", "Hoang", "Huynh", "Phan", "Vu", "Vo", "Dang",
        "Bui", "Do", "Ho", "Ngo", "Duong", "Ly", "An", "Binh", "Chau", "Danh",
        "Dat", "Dung", "Giang", "Ha", "Hai", "Hien", "Hiep", "Hoa", "Hung", "Huy",
        "Khanh", "Khoa", "Lam", "Linh", "Long", "Minh", "Nam", "Nga", "Phong", "Quan",
        "Quang", "Son", "Tam", "Thao", "Thanh", "Thien", "Thinh", "Tien", "Tu", "Tuan"
    )

    val lastNames = listOf(
        "Van", "Thi", "Minh", "Anh", "Bao", "Cong", "Duc", "Duy", "Gia", "Hai",
        "Hoang", "Hong", "Huu", "Khac", "Khanh", "Lan", "Linh", "Long", "Ngoc", "Nhat",
        "Phuoc", "Quan", "Quoc", "Tam", "Thanh", "The", "Thien", "Thu", "Thuy", "Tien",
        "Trong", "Trung", "Tu", "Tuan", "Tung", "Vinh", "Xuan", "Yen", "Phuong", "Cuong",
        "Diep", "Kien", "Oanh", "Tram", "Trang", "Tri", "Phuc", "Loc", "Tho", "Khang"
    )

    val basePasswords = List(50) { _ -> "123456" }

    // 2. Lấy timestamp hiện tại để làm hậu tố độc nhất
    val timestamp = currentTimeMillis()

    return List(count) { index ->
        val i = index % 50 // Đảm bảo index luôn nằm trong khoảng 0-49

        val rawUsername = "${firstNames[i].lowercase(getDefault())}_${
            lastNames[i].lowercase(
                getDefault()
            )
        }"
        val uniqueUsername = "${rawUsername}_${timestamp}_$index"
        val uniqueEmail = "${rawUsername}_${timestamp}_$index@gmail.com"
        val displayName = "${firstNames[i]} ${lastNames[i]}"
        val password = basePasswords[i]

        RegisterRequest(
            email = uniqueEmail,
            username = uniqueUsername,
            password = password,
            displayName = displayName
        )
    }
}

operator fun String.times(n: Int) = this.repeat(n)