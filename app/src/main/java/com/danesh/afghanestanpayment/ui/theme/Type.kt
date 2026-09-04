package com.danesh.afghanestanpayment.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import com.danesh.afghanestanpayment.R
import com.danesh.ui.theme.AppFontFamilies
import com.danesh.ui.theme.appTypography

private val PreviewFontFamilies = AppFontFamilies(
    rtl = FontFamily(Font(R.font.iranyekanblackfanum)),
    ltr = FontFamily(Font(R.font.inter)),
)

/** پیش‌فرض RTL — برای Preview */
val Typography: Typography = appTypography(PreviewFontFamilies, LayoutDirection.Rtl)
