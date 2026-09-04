package com.danesh.settings.model

import androidx.annotation.StringRes
import com.danesh.settings.R

enum class AppThemeMode(
    @StringRes val labelRes: Int,
) {
    Dark(R.string.settings_theme_dark),
    Light(R.string.settings_theme_light),
}
