package com.danesh.report.model

import androidx.annotation.StringRes
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.report.R

/**
 * چیپ فیلتر نوع تراکنش در گزارش.
 * [featureName] باید با نام آیتم در `BuildConfig.ENABLED_FEATURES` / [MenuItemType] یکی باشد.
 */
enum class ReportTransactionChipFilter(
    @StringRes val labelRes: Int,
    val featureName: String? = null,
) {
    ALL(R.string.report_status_all),
    PURCHASE(com.danesh.common.R.string.tx_type_purchase, "PURCHASE"),
    BILL(R.string.report_chip_bill, "BILL"),
    TOPUP(R.string.report_chip_topup, "TOPUP"),
    VOUCHER(com.danesh.common.R.string.tx_type_voucher, "VOUCHER"),
    TRANSFER(R.string.report_chip_transfer, "TRANSFER"),
    CASH_DEPOSIT(com.danesh.common.R.string.tx_type_cash_deposit, "CASH_DEPOSIT"),
    CASH_OUT(com.danesh.common.R.string.tx_type_cash_out, "CASH_OUT"),
    BALANCE(com.danesh.common.R.string.tx_type_balance, "BALANCE"),
    SUPPORT(com.danesh.common.R.string.tx_type_support, "SUPPORT");


    companion object {
        /** فقط «همه» + انواع تراکنشی که در ENABLED_FEATURES همان PSP فعال‌اند (بدون موجودی). */
        fun visibleFor(enabledFeatures: Set<String>): List<ReportTransactionChipFilter> {
            val withoutBalance = entries.filter { it != BALANCE }
            if (enabledFeatures.isEmpty()) {
                return withoutBalance.filter { it == ALL || it.featureName != null }
            }
            return withoutBalance.filter { chip ->
                chip.featureName == null || chip.featureName in enabledFeatures
            }
        }
    }
}

fun TransactionResultDetail.matchesChipFilter(chip: ReportTransactionChipFilter): Boolean = when (chip) {
    ReportTransactionChipFilter.ALL -> true
    ReportTransactionChipFilter.PURCHASE ->
        transactionType == TransactionType.PURCHASE && !isLikelyTopUp()
    ReportTransactionChipFilter.VOUCHER -> transactionType == TransactionType.VOUCHER
    ReportTransactionChipFilter.BILL -> transactionType == TransactionType.BILL
    ReportTransactionChipFilter.TOPUP ->
        transactionType == TransactionType.TOPUP || isLikelyTopUp()
    ReportTransactionChipFilter.TRANSFER ->
        transactionType == TransactionType.CARD_TO_CARD ||
            transactionType == TransactionType.CARD_TO_WALLET ||
            transactionType == TransactionType.WALLET_TO_WALLET
    ReportTransactionChipFilter.CASH_DEPOSIT -> transactionType == TransactionType.CASH_DEPOSIT
    ReportTransactionChipFilter.CASH_OUT -> transactionType == TransactionType.CASH_OUT
    ReportTransactionChipFilter.BALANCE -> transactionType == TransactionType.BALANCE
    ReportTransactionChipFilter.SUPPORT -> transactionType == TransactionType.SUPPORT
}

private fun TransactionResultDetail.isLikelyTopUp(): Boolean =
    posCode.contains("topup", ignoreCase = true) ||
        merchantName.contains("شارژ", ignoreCase = true)
