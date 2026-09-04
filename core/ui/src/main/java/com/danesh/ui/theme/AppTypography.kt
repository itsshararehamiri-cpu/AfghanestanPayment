package com.danesh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class AppFontFamilies(
    val rtl: FontFamily,
    val ltr: FontFamily,
)

val LocalAppFontFamilies = staticCompositionLocalOf {
    AppFontFamilies(
        rtl = FontFamily.Default,
        ltr = FontFamily.Default,
    )
}

/** جهت UI برنامه بر اساس زبان — بدون تأثیر از [NumericInputContainer] که LTR موقت می‌گذارد. */
val LocalAppUiLayoutDirection = staticCompositionLocalOf { LayoutDirection.Rtl }

fun AppFontFamilies.forLayoutDirection(layoutDirection: LayoutDirection): FontFamily = when (layoutDirection) {
    LayoutDirection.Rtl -> rtl
    LayoutDirection.Ltr -> ltr
}

fun appTypography(
    fontFamilies: AppFontFamilies,
    layoutDirection: LayoutDirection,
): Typography {
    val fontFamily = fontFamilies.forLayoutDirection(layoutDirection)
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
    ).withFontFamily(fontFamily)
}

private fun Typography.withFontFamily(fontFamily: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = fontFamily),
    displayMedium = displayMedium.copy(fontFamily = fontFamily),
    displaySmall = displaySmall.copy(fontFamily = fontFamily),
    headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
    titleLarge = titleLarge.copy(fontFamily = fontFamily),
    titleMedium = titleMedium.copy(fontFamily = fontFamily),
    titleSmall = titleSmall.copy(fontFamily = fontFamily),
    bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = bodySmall.copy(fontFamily = fontFamily),
    labelLarge = labelLarge.copy(fontFamily = fontFamily),
    labelMedium = labelMedium.copy(fontFamily = fontFamily),
    labelSmall = labelSmall.copy(fontFamily = fontFamily),
)

@Composable
fun appFontFamily(): FontFamily =
    LocalAppFontFamilies.current.forLayoutDirection(LocalAppUiLayoutDirection.current)

@Composable
fun appLtrFontFamily(): FontFamily = LocalAppFontFamilies.current.ltr

@Composable
fun TextStyle.withAppFont(): TextStyle = copy(fontFamily = appFontFamily())

@Composable
fun appTextStyle(
    base: TextStyle = MaterialTheme.typography.bodyMedium,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    textDirection: TextDirection? = null,
): TextStyle = base.withAppFont().let { styled ->
    styled.copy(
        fontSize = if (fontSize != TextUnit.Unspecified) fontSize else styled.fontSize,
        fontWeight = fontWeight ?: styled.fontWeight,
        textAlign = textAlign ?: styled.textAlign,
        textDirection = textDirection ?: styled.textDirection,
    )
}
