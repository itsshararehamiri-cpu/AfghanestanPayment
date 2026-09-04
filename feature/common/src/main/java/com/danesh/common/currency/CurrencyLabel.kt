package com.danesh.common.currency

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

/**
 * برچسب واحد پول UI — از [BuildConfig.CURRENCY_LABEL] در Activity (flavor PSP) تزریق می‌شود.
 */
val LocalCurrencyLabel = staticCompositionLocalOf { DEFAULT_CURRENCY_LABEL }

private const val DEFAULT_CURRENCY_LABEL = "AFN"

@Composable
fun currencyLabel(): String = LocalCurrencyLabel.current

@Composable
fun amountWithCurrency(amount: String): String =
    stringResource(R.string.amount_with_currency, amount, currencyLabel())

@Composable
fun amountFieldLabel(): String =
    stringResource(R.string.amount_currency, currencyLabel())

@Composable
fun topUpAmountFieldLabel(): String =
    stringResource(R.string.topup_amount_label, currencyLabel())

@Composable
fun placeholderAmountUnavailable(): String =
    stringResource(R.string.placeholder_amount_unavailable, currencyLabel())

fun formatAmountWithCurrencyLabel(amount: String, currencyLabel: String): String {
    val normalized = amount.replace(",", "").trim()
    if (normalized.isEmpty() || normalized == "—") {
        return "— $currencyLabel"
    }
    return "$amount $currencyLabel"
}
