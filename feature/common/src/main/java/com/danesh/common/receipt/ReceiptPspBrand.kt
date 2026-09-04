package com.danesh.common.receipt

import androidx.annotation.DrawableRes
import androidx.compose.runtime.staticCompositionLocalOf
import com.danesh.common.R

enum class ReceiptPspBrand {
    HP,
    FANAVA,
    AP,
    PN,
    BP,
    SADAD
}

val LocalReceiptPspBrand = staticCompositionLocalOf { ReceiptPspBrand.HP }

data class PspReceiptLogoAssets(
    @DrawableRes val networkLogo: Int,
    @DrawableRes val brandLogo: Int,
    @DrawableRes val brandLogoPaper: Int,
    val tintBrandOnPaper: Boolean = false,
    val tintNetworkLogo: Boolean = true,
)

fun ReceiptPspBrand.receiptLogoAssets(): PspReceiptLogoAssets = when (this) {
    ReceiptPspBrand.BP -> PspReceiptLogoAssets(
        networkLogo = R.drawable.ic_new_shapark,//logo_sha
        brandLogo = R.drawable.beh_logo,
        brandLogoPaper = R.drawable.logo_bpm_w,
        tintNetworkLogo = true,
    )
    ReceiptPspBrand.HP -> PspReceiptLogoAssets(
        networkLogo = R.drawable.ic_hp,
        brandLogo = R.drawable.ic_hamrahpay_new,
        brandLogoPaper = R.drawable.hp_logo,
        tintNetworkLogo = true,
    )
    ReceiptPspBrand.AP -> PspReceiptLogoAssets(
        networkLogo = R.drawable.ic_new_shapark,
        brandLogo = R.drawable.beh_logo,
        brandLogoPaper = R.drawable.logo_bpm_b,
        tintNetworkLogo = true,
    )
    ReceiptPspBrand.FANAVA -> PspReceiptLogoAssets(
        networkLogo = R.drawable.ic_new_shapark,
        brandLogo = R.drawable.beh_logo,
        brandLogoPaper = R.drawable.logo_bpm_b,
        tintNetworkLogo = true,
    )
    ReceiptPspBrand.PN -> PspReceiptLogoAssets(
        networkLogo = R.drawable.ic_new_shapark,
        brandLogo = R.drawable.logo_pn_white,
        brandLogoPaper = R.drawable.logo_bpm_b,
        tintBrandOnPaper = true,
        tintNetworkLogo = true,
    )
    ReceiptPspBrand.SADAD -> PspReceiptLogoAssets(
        networkLogo = R.drawable.sadad,
        brandLogo = R.drawable.sadad,
        brandLogoPaper = R.drawable.sadad,
        tintBrandOnPaper = true,
        tintNetworkLogo = true,
    )
}

@DrawableRes
fun ReceiptPspBrand.brandLogoRes(): Int = receiptLogoAssets().brandLogo
