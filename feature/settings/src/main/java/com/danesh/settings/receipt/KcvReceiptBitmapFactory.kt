package com.danesh.settings.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.danesh.common.receipt.ReceiptPspBrand
import com.danesh.common.receipt.paper.PaperReceiptBitmapFonts
import com.danesh.common.receipt.paper.PaperReceiptPspLogoDrawer

object KcvReceiptBitmapFactory {

    const val WIDTH = 384
    private const val TOP_PADDING = 24
    private const val TITLE_SIZE = 28f
    private const val LINE_SIZE = 24f
    private const val LINE_SPACING = 40f
    private const val TITLE_BOTTOM_SPACING = 36f

    fun create(
        context: Context,
        title: String,
        macKeyLabel: String,
        macKeyValue: String,
        dataKeyLabel: String,
        dataKeyValue: String,
        pinKeyLabel: String,
        pinKeyValue: String,
        fonts: PaperReceiptBitmapFonts,
        pspBrand: ReceiptPspBrand,
    ): Bitmap {
        val lines = listOf(
            "$macKeyLabel      $macKeyValue",
            "$dataKeyLabel      $dataKeyValue",
            "$pinKeyLabel      $pinKeyValue",
        )
        val logoBlockHeight = PaperReceiptPspLogoDrawer.logoBlockHeight(context, pspBrand, WIDTH)
        val height = (
            TOP_PADDING + TITLE_SIZE + TITLE_BOTTOM_SPACING +
                lines.size * LINE_SPACING + logoBlockHeight
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

        PaperReceiptPspLogoDrawer.drawLogo(
            context = context,
            canvas = canvas,
            brand = pspBrand,
            receiptWidth = WIDTH,
            startY = y,
        )

        return bitmap
    }
}
