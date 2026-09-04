package com.danesh.menu.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

@Composable
fun rememberMenuQrCodeBitmap(
    content: String,
    size: Int = 512,
    foregroundColor: Color = Color(0xFF5FFBF3),
    backgroundColor: Color = Color.Transparent,
): Bitmap? {
    return remember(content, size, foregroundColor, backgroundColor) {
        if (content.isBlank()) return@remember null
        generateMenuQrCodeBitmap(content, size, foregroundColor, backgroundColor)
    }
}

private fun generateMenuQrCodeBitmap(
    content: String,
    size: Int,
    foregroundColor: Color,
    backgroundColor: Color,
): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 0)
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val bitmap = createBitmap(size, size)
    val foreground = foregroundColor.toArgb()
    val background = backgroundColor.toArgb()

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap[x, y] = if (matrix[x, y]) foreground else background
        }
    }
    return bitmap
}
