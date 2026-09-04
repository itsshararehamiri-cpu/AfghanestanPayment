package com.danesh.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

private val AppColorScheme = darkColorScheme(
    primary = AppColors.Accent,
    onPrimary = AppColors.ScreenBackground,
    secondary = AppColors.AccentSecondary,
    onSecondary = AppColors.ScreenBackground,
    background = AppColors.ScreenBackground,
    surface = AppColors.ScreenBackground,
    onBackground = AppColors.TextOnBackground,
    onSurface = AppColors.TextOnBackground,
    error = AppColors.Error,
    onError = AppColors.TextOnBackground,
)

@Composable
fun AppTheme(
    typography: Typography? = null,
    content: @Composable () -> Unit,
) {
    val layoutDirection = LocalAppUiLayoutDirection.current
    val fontFamilies = LocalAppFontFamilies.current
    val resolvedTypography = typography ?: remember(layoutDirection, fontFamilies) {
        appTypography(fontFamilies, layoutDirection)
    }
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = resolvedTypography,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides resolvedTypography.bodySmall.copy(
                color = AppColors.TextOnBackground,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .appScreenBackground(),
            ) {
                content()
            }
        }
    }
}

fun Modifier.appScreenBackground(): Modifier =
    background(AppColors.ScreenBackground)
