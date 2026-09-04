package com.danesh.settings.ui.theme

import androidx.compose.ui.graphics.Color
import com.danesh.ui.theme.AppColors

object SettingsColors {
    val Background = AppColors.ScreenBackground
    val TextPrimary = AppColors.TextOnBackground
    val Accent = AppColors.Accent
    val OptionBorder = AppColors.AccentSecondary
    val OptionBackground = AppColors.ScreenBackground.copy(alpha = 0.19f)
    val CloseBorder = Color(0xFF144B5B)
    val DragHandle = Color(0xFF848484)
    val RadioUnselected = AppColors.Accent
}
