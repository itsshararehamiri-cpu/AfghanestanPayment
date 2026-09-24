package com.danesh.common.receipt

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@StringRes
fun voucherOperatorNameRes(operatorCode: String, brand: ReceiptPspBrand = ReceiptPspBrand.BP): Int? =
    when (brand) {
        ReceiptPspBrand.HP -> hpVoucherOperatorNameRes(operatorCode)
        ReceiptPspBrand.SADAD -> sadadVoucherOperatorNameRes(operatorCode)
        else -> bpVoucherOperatorNameRes(operatorCode)
    }

@StringRes
private fun bpVoucherOperatorNameRes(operatorCode: String): Int? = when (operatorCode.trim()) {
    "1" -> R.string.voucher_operator_hamrah_e_aval
    "2" -> R.string.voucher_operator_irancell
    "3" -> R.string.voucher_operator_rightel
    "4" -> R.string.voucher_operator_talia
    else -> null
}

@StringRes
private fun hpVoucherOperatorNameRes(operatorCode: String): Int? = when (operatorCode.trim()) {
    "1" -> R.string.voucher_operator_roshan
    "2" -> R.string.voucher_operator_mtn
    "3" -> R.string.voucher_operator_awcc
    "4" -> R.string.voucher_operator_etisalat
    else -> null
}

@StringRes
private fun sadadVoucherOperatorNameRes(operatorCode: String): Int? = when (operatorCode.trim()) {
    "919", "1" -> R.string.voucher_operator_hamrah_e_aval
    "935", "2" -> R.string.voucher_operator_irancell
    "921", "3" -> R.string.voucher_operator_rightel
    "932", "4" -> R.string.voucher_operator_talia
    else -> null
}

@Composable
fun voucherChargeMethodLabel(
    operatorCode: String,
    brand: ReceiptPspBrand = LocalReceiptPspBrand.current,
): String {
    val trimmed = operatorCode.trim()
    if (trimmed.isEmpty()) return ""
    return voucherOperatorNameRes(trimmed, brand)?.let { stringResource(it) } ?: trimmed
}

fun ReceiptType.showsVoucherChargeDetails(): Boolean = this != ReceiptType.MERCHANT_RECEIPT
