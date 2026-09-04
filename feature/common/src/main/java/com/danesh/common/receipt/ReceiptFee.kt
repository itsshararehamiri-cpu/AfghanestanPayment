package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * کارمزد استعلام موجودی روی رسید — از BuildConfig flavor PSP در Activity تزریق می‌شود.
 */
val LocalBalanceTransactionFee = staticCompositionLocalOf { DEFAULT_BALANCE_TRANSACTION_FEE }

private const val DEFAULT_BALANCE_TRANSACTION_FEE = "1800"

@Composable
fun balanceTransactionFee(): String = LocalBalanceTransactionFee.current
