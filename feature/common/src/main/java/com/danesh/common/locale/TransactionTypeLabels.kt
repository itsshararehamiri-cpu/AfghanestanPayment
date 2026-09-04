package com.danesh.common.locale

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.danesh.api.TransactionType
import com.danesh.common.R

@StringRes
fun TransactionType.titleRes(): Int = when (this) {
    TransactionType.BALANCE -> R.string.tx_type_balance
    TransactionType.PURCHASE -> R.string.tx_type_purchase
    TransactionType.CARD_TO_CARD -> R.string.tx_type_card_to_card
    TransactionType.CARD_TO_WALLET -> R.string.tx_type_card_to_wallet
    TransactionType.WALLET_TO_WALLET -> R.string.tx_type_wallet_to_wallet
    TransactionType.BILL -> R.string.tx_type_bill
    TransactionType.ADVICE -> R.string.tx_type_advice
    TransactionType.SETTLEMENT -> R.string.tx_type_settlement
    TransactionType.CASH_DEPOSIT -> R.string.tx_type_cash_deposit
    TransactionType.CASH_OUT -> R.string.tx_type_cash_out
    TransactionType.LOGON -> R.string.tx_type_logon
    TransactionType.INIT -> R.string.tx_type_init
    TransactionType.VOUCHER -> R.string.tx_type_voucher
    TransactionType.SUPPORT -> R.string.tx_type_support
    TransactionType.TOPUP -> R.string.tx_type_topup
    TransactionType.SIGNON -> com.danesh.common.R.string.tx_type_init
    TransactionType.TERMINAL_CONFIG -> com.danesh.common.R.string.tx_type_init

}

@Composable
@ReadOnlyComposable
fun TransactionType.localizedTitle(): String = stringResource(titleRes())
