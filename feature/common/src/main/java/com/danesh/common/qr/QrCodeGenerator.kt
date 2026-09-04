package com.danesh.common.qr


import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun rememberQrCodeBitmap(
    content: String,
    size: Int = 512,
    foregroundColor: Color = Color(0xFF00FFD4),
) = remember(content, size, foregroundColor) {
    generateQrCodeBitmap(content, size, foregroundColor)
}

fun generateQrCodeBitmap(
    content: String,
    size: Int = 512,
    foregroundColor: Color = Color(0xFF00FFD4),
): ImageBitmap {
    val hints = hashMapOf<EncodeHintType, kotlin.Any>(EncodeHintType.MARGIN to 0)
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val onColor = foregroundColor.toArgb()
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        for (x in 0 until size) {
            pixels[y * size + x] = if (matrix.get(x, y)) onColor else android.graphics.Color.TRANSPARENT
        }
    }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
}

//import android.graphics.Bitmap
////import android.graphics.Color
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.graphics.toArgb
//import com.google.zxing.BarcodeFormat
//import com.google.zxing.EncodeHintType
//import com.google.zxing.qrcode.QRCodeWriter
//
//@Composable
//fun rememberQrCodeBitmap(
//    content: String,
//    size: Int = 512,
//    foregroundColor: Color = Color(0xFF00FFD4),
//) = remember(content, size, foregroundColor) {
//    generateQrCodeBitmap(content, size, foregroundColor)
//}
//
//fun generateQrCodeBitmap(
//    content: String,
//    size: Int = 512,
//    foregroundColor: Color = Color(0xFF00FFD4),
//): androidx.compose.ui.graphics.ImageBitmap {
//    val hints = hashMapOf<EncodeHintType, Any>(EncodeHintType.MARGIN to 0)
//    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
//    val onColor = foregroundColor.toArgb()
//    val pixels = IntArray(size * size)
//    for (y in 0 until size) {
//        for (x in 0 until size) {
//            pixels[y * size + x] = if (matrix.get(x, y)) onColor else Color.Transparent
//        }
//    }
//    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
//}
