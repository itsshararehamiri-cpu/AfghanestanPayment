package com.danesh.settings.model

import androidx.annotation.StringRes
import com.danesh.settings.R

enum class AppFontFamily(
    @StringRes val labelRes: Int,
) {
    YekanBakh(R.string.settings_font_yekan_bakh),
    SansSerif(R.string.settings_font_sans_serif),
}
