package com.danesh.common.receipt.paper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.DrawableRes
import com.danesh.common.receipt.ReceiptPspBrand
import com.danesh.common.receipt.receiptLogoAssets
import kotlin.math.min

object PaperReceiptPspLogoDrawer {
    private const val LOGO_TOP_SPACING_PX = 16f
    private const val BOTTOM_PADDING_PX = 24f
    private const val HP_LOGO_HEIGHT_DP = 100f
    private const val HP_LOGO_WIDTH_DP = 130f
    private const val HP_LOGO_WIDTH_TINTED_DP = 150f

    fun logoBlockHeight(context: Context, brand: ReceiptPspBrand, receiptWidth: Int): Float {
        val logo = loadScaledLogo(context, brand, receiptWidth) ?: return 0f
        return LOGO_TOP_SPACING_PX + logo.height + BOTTOM_PADDING_PX
    }

    fun drawLogo(
        context: Context,
        canvas: Canvas,
        brand: ReceiptPspBrand,
        receiptWidth: Int,
        startY: Float,
    ) {
        val logo = loadScaledLogo(context, brand, receiptWidth) ?: return
        val left = when (brand) {
            ReceiptPspBrand.HP -> (receiptWidth - logo.width) / 2f
            else -> 0f
        }
        canvas.drawBitmap(
            logo,
            left,
            startY + LOGO_TOP_SPACING_PX,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
    }

    private fun loadScaledLogo(
        context: Context,
        brand: ReceiptPspBrand,
        receiptWidth: Int,
    ): Bitmap? {
        val assets = brand.receiptLogoAssets()
        val source = decode(context, assets.brandLogoPaper) ?: return null
        return when (brand) {
            ReceiptPspBrand.HP -> scaleToBounds(
                source,
                maxWidth = dp(
                    context,
                    if (assets.tintBrandOnPaper) HP_LOGO_WIDTH_TINTED_DP else HP_LOGO_WIDTH_DP,
                ),
                maxHeight = dp(context, HP_LOGO_HEIGHT_DP),
            )
            else -> scaleToWidth(source, receiptWidth)
        }
    }

    private fun dp(context: Context, value: Float): Int =
        (value * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)

    private fun decode(context: Context, @DrawableRes resId: Int): Bitmap? =
        BitmapFactory.decodeResource(context.resources, resId)

    private fun scaleToWidth(source: Bitmap, targetWidth: Int): Bitmap {
        if (source.width <= 0) return source
        val targetHeight = (source.height.toFloat() / source.width * targetWidth)
            .toInt()
            .coerceAtLeast(1)
        if (source.width == targetWidth && source.height == targetHeight) return source
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }

    private fun scaleToBounds(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (source.width <= 0 || source.height <= 0) return source
        val ratio = min(
            maxWidth.toFloat() / source.width,
            maxHeight.toFloat() / source.height,
        )
        val width = (source.width * ratio).toInt().coerceAtLeast(1)
        val height = (source.height * ratio).toInt().coerceAtLeast(1)
        if (source.width == width && source.height == height) return source
        return Bitmap.createScaledBitmap(source, width, height, true)
    }
}
