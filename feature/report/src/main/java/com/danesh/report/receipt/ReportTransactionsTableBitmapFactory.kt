package com.danesh.report.receipt

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.danesh.common.receipt.paper.PaperReceiptBitmapFonts

object ReportTransactionsTableBitmapFactory {

    private const val WIDTH = 384
    private const val HORIZONTAL_PADDING = 8f
    private const val TOP_PADDING = 24f
    private const val TITLE_SIZE = 26f
    private const val HEADER_SIZE = 13f
    private const val ROW_SIZE = 12f
    private const val TITLE_BOTTOM_SPACING = 28f
    private const val HEADER_BOTTOM_SPACING = 10f
    private const val ROW_HEIGHT = 34f
    private const val ROW_GAP = 4f
    private const val BOTTOM_PADDING = 24f

    private val columnWidths = floatArrayOf(72f, 68f, 52f, 72f, 96f)

    fun create(
        title: String,
        typeColumn: String,
        dateColumn: String,
        timeColumn: String,
        amountColumn: String,
        referenceColumn: String,
        rows: List<ReportTableRow>,
        emptyMessage: String,
        fonts: PaperReceiptBitmapFonts,
    ): Bitmap {
        val header = ReportTableRow(
            type = typeColumn,
            date = dateColumn,
            time = timeColumn,
            amount = amountColumn,
            reference = referenceColumn,
        )
        val contentRows = if (rows.isEmpty()) {
            listOf(ReportTableRow("—", "—", "—", "—", emptyMessage))
        } else {
            rows
        }

        val height = (
            TOP_PADDING + TITLE_SIZE + TITLE_BOTTOM_SPACING +
                ROW_HEIGHT + HEADER_BOTTOM_SPACING +
                contentRows.size * (ROW_HEIGHT + ROW_GAP) +
                BOTTOM_PADDING
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
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = HEADER_SIZE
            color = Color.BLACK
            typeface = fonts.titleTypeface
            textAlign = fonts.lineTextAlign
        }
        val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = ROW_SIZE
            color = Color.BLACK
            typeface = fonts.bodyTypeface
            textAlign = fonts.lineTextAlign
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 1f
        }

        var y = TOP_PADDING + TITLE_SIZE
        canvas.drawText(title, WIDTH / 2f, y, titlePaint)
        y += TITLE_BOTTOM_SPACING

        drawRow(canvas, header, y, headerPaint, fonts.lineTextAlign)
        y += ROW_HEIGHT
        canvas.drawLine(
            HORIZONTAL_PADDING,
            y,
            WIDTH - HORIZONTAL_PADDING,
            y,
            linePaint,
        )
        y += HEADER_BOTTOM_SPACING

        contentRows.forEach { row ->
            drawRow(canvas, row, y + ROW_SIZE, rowPaint, fonts.lineTextAlign)
            y += ROW_HEIGHT + ROW_GAP
        }

        return bitmap
    }

    private fun drawRow(
        canvas: Canvas,
        row: ReportTableRow,
        baselineY: Float,
        paint: Paint,
        textAlign: Paint.Align,
    ) {
        val values = listOf(row.type, row.date, row.time, row.amount, row.reference)
        if (textAlign == Paint.Align.RIGHT) {
            var xRight = WIDTH - HORIZONTAL_PADDING
            values.forEachIndexed { index, value ->
                val maxWidth = columnWidths[index]
                val text = truncate(value, paint, maxWidth)
                canvas.drawText(text, xRight, baselineY, paint)
                xRight -= maxWidth
            }
        } else {
            var xLeft = HORIZONTAL_PADDING
            values.forEachIndexed { index, value ->
                val maxWidth = columnWidths[index]
                val text = truncate(value, paint, maxWidth)
                canvas.drawText(text, xLeft, baselineY, paint)
                xLeft += maxWidth
            }
        }
    }

    private fun truncate(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var trimmed = text
        while (trimmed.length > 1 && paint.measureText("$trimmed…") > maxWidth) {
            trimmed = trimmed.dropLast(1)
        }
        return "$trimmed…"
    }
}
