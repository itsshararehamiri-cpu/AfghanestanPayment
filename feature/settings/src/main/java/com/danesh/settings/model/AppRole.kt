package com.danesh.settings.model

import androidx.annotation.StringRes
import com.danesh.settings.R

enum class AppRole(
    @StringRes val labelRes: Int,
) {
    Support(R.string.settings_role_support),
    Merchant(R.string.settings_role_merchant),
}
