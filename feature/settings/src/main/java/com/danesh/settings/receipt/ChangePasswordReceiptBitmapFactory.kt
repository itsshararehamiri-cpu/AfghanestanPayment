package com.danesh.settings.receipt

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.danesh.common.receipt.paper.PaperReceiptBitmapFonts

object ChangePasswordReceiptBitmapFactory {

    private const val WIDTH = 384
    private const val HORIZONTAL_PADDING = 16
    private const val TOP_PADDING = 24
    private const val TITLE_SIZE = 28f
    private const val LINE_SIZE = 24f
    private const val LINE_SPACING = 40f
    private const val TITLE_BOTTOM_SPACING = 36f

    fun create(
        title: String,
        dateTimeLabel: String,
        dateTime: String,
        successLabel: String,
        fonts: PaperReceiptBitmapFonts,
    ): Bitmap {
        val lines = listOf(
            "$dateTimeLabel $dateTime",
            successLabel,
        )
        val height = (
            TOP_PADDING + TITLE_SIZE + TITLE_BOTTOM_SPACING +
                lines.size * LINE_SPACING + TOP_PADDING
            ).toInt()

        val bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = TITLE_SIZE
            color = Color.BLACK
            typeface = fonts.titleTypeface
            textAlign = Paint.Align.CENTER
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = LINE_SIZE
            color = Color.BLACK
            typeface = fonts.bodyTypeface
            textAlign = fonts.lineTextAlign
        }

        var y = TOP_PADDING + TITLE_SIZE
        canvas.drawText(title, WIDTH / 2f, y, titlePaint)
        y += TITLE_BOTTOM_SPACING

        lines.forEach { line ->
            canvas.drawText(line, fonts.lineX, y, linePaint)
            y += LINE_SPACING
        }

        return bitmap
    }
}
