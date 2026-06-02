package com.example.se405.android.core.utils

import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

fun openDownloadedFile(context: Context, localFile: File) {
    try {
        // 1. Lấy đuôi file để phân tích định dạng (MimeType)
        val extension = localFile.extension.lowercase()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

        // 2. Chuyển đổi cấu trúc File thành một Content URI bảo mật thông qua FileProvider
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            localFile
        )

        // 3. Xây dựng Intent hành động XEM dữ liệu
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, mimeType)
            // Cực kỳ quan trọng: Cấp quyền đọc file tạm thời cho ứng dụng được gọi
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // 4. Hiển thị hộp thoại "Mở bằng..." (App Chooser) để người dùng lựa chọn ứng dụng
        val chooserIntent = Intent.createChooser(intent, "Mở tài liệu bằng...")
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        e.printStackTrace()
        // Xử lý thông báo lỗi nếu trên thiết bị không có ứng dụng nào đọc được định dạng file này
    }
}