package com.danesh.common.currency

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import com.danesh.common.R
import com.danesh.common.locale.AppLocale
import com.danesh.common.locale.LocalReceiptLocale

/**
 * برچسب واحد پول UI — از [BuildConfig.CURRENCY_LABEL] در Activity (flavor PSP) تزریق می‌شود.
 */
val LocalCurrencyLabel = staticCompositionLocalOf { DEFAULT_CURRENCY_LABEL }

private const val DEFAULT_CURRENCY_LABEL = "AFN"
private const val RIAL_LABEL = "ریال"
private const val RIAL_LABEL_EN = "IRR"

@Composable
fun currencyLabel(): String =
    localizedCurrencyLabel(LocalCurrencyLabel.current, LocalReceiptLocale.current)

/** برچسب ریال در زبان انگلیسی به‌صورت IRR نمایش داده می‌شود. */
fun localizedCurrencyLabel(label: String, locale: AppLocale?): String =
    if (locale == AppLocale.ENGLISH && label == RIAL_LABEL) RIAL_LABEL_EN else label

/**
 * مبلغ به حروف همراه با واحد پول (مثلاً «یک هزار و دویست ریال»)؛ برای ورودی خالی `null`.
 */
@Composable
fun amountInWordsWithCurrency(amount: String): String? {
    val words = AmountInWords.convert(amount, LocalReceiptLocale.current) ?: return null
    return "$words ${currencyLabel()}"
}

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
