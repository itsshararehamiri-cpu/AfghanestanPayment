package com.danesh.menu.ui

import androidx.annotation.StringRes
import com.danesh.common.receipt.ReceiptPspBrand
import com.danesh.menu.R

@StringRes
fun ReceiptPspBrand.menuBrandNameRes(): Int = when (this) {
    ReceiptPspBrand.HP -> R.string.menu_app_name
    ReceiptPspBrand.AP -> R.string.menu_app_name_ap
    ReceiptPspBrand.FANAVA -> R.string.menu_app_name_fanava
    ReceiptPspBrand.PN -> R.string.menu_app_name_pn
    ReceiptPspBrand.BP -> R.string.menu_app_name_behpardakht
    ReceiptPspBrand.SADAD -> R.string.menu_app_name_sadad

}
