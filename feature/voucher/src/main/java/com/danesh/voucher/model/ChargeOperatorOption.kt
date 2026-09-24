package com.danesh.voucher.model

import androidx.annotation.DrawableRes
import com.danesh.voucher.R

data class ChargeOperatorOption(
    val id: String,
    val displayName: String,
    @DrawableRes val logoRes: Int,
)

fun sadadOperatorLogo(providerId: String): Int = when (providerId) {
    "919" -> R.drawable.ic_hamraheaval
    "935" -> R.drawable.ic_irancel
    "921" -> R.drawable.ic_ritel
    else -> R.drawable.ic_irancel
}
