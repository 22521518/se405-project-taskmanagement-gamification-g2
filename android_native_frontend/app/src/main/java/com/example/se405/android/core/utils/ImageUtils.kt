package com.example.se405.android.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

object ImageUtils {
    fun compressUriToByteArray(context: Context, uri: Uri, maxSize: Int = 500): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream?.close()

            // Tính toán tỷ lệ thu nhỏ (chỉ thu nhỏ nếu ảnh lớn hơn maxSize)
            val ratio = minOf(maxSize.toFloat() / originalBitmap.width, maxSize.toFloat() / originalBitmap.height)
            val scaledBitmap = if (ratio < 1f) {
                originalBitmap.scale(
                    (originalBitmap.width * ratio).toInt(),
                    (originalBitmap.height * ratio).toInt()
                )
            } else {
                originalBitmap
            }

            // Nén ra mảng byte (JPEG 80% dung lượng siêu nhẹ, chất lượng đủ nhìn)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}