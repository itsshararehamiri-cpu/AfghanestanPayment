package com.danesh.common.receipt.paper

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.danesh.common.locale.AppLayoutDirection
import com.danesh.common.locale.LocalePreferences
import javax.inject.Inject
import javax.inject.Singleton

data class PaperReceiptBitmapFonts(
    val titleTypeface: Typeface,
    val bodyTypeface: Typeface,
    val lineTextAlign: Paint.Align,
    val lineX: Float,
)

@Singleton
class PaperReceiptTypefaceResolver @Inject constructor(
    private val localePreferences: LocalePreferences,
) {
    fun isRtl(): Boolean =
        localePreferences.getLanguage().layoutDirection == AppLayoutDirection.Rtl

    fun titleTypeface(context: Context): Typeface =
        Typeface.create(bodyTypeface(context), Typeface.BOLD)

    fun bodyTypeface(context: Context): Typeface {
        val fontName = when (localePreferences.getLanguage().layoutDirection) {
            AppLayoutDirection.Rtl -> RTL_FONT
            AppLayoutDirection.Ltr -> LTR_FONT
        }
        return loadFont(context, fontName) ?: Typeface.DEFAULT
    }

    fun lineTextAlign(): Paint.Align =
        if (isRtl()) Paint.Align.RIGHT else Paint.Align.LEFT

    fun lineX(width: Int, horizontalPadding: Int): Float =
        if (isRtl()) (width - horizontalPadding).toFloat() else horizontalPadding.toFloat()

    fun bitmapFonts(
        context: Context,
        width: Int,
        horizontalPadding: Int,
    ): PaperReceiptBitmapFonts = PaperReceiptBitmapFonts(
        titleTypeface = titleTypeface(context),
        bodyTypeface = bodyTypeface(context),
        lineTextAlign = lineTextAlign(),
        lineX = lineX(width, horizontalPadding),
    )

    private fun loadFont(context: Context, fontName: String): Typeface? {
        val resId = context.resources.getIdentifier(fontName, "font", context.packageName)
        if (resId == 0) return null
        return ResourcesCompat.getFont(context, resId)
    }

    companion object {
        private const val RTL_FONT = "iranyekanblackfanum"
        private const val LTR_FONT = "inter"
    }
}
