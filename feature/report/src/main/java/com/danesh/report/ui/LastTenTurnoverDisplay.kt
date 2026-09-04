package com.danesh.report.ui

import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.common.locale.AppLocale
import com.danesh.common.locale.ReceiptCalendarStyle
import com.danesh.common.locale.TransactionDateTimeFormatter

data class LastTenTurnoverDetailLine(
    val label: String,
    val value: String,
    val valueLtr: Boolean = false,
) {
    fun displayText(): String = when {
        label.isBlank() -> value
        value.isBlank() -> label
        else -> "$label $value"
    }
}

data class LastTenTurnoverDisplay(
    val dateTimeLine: String,
    val typeLine: String,
    val detailLines: List<LastTenTurnoverDetailLine>,
    val amountLine: String,
)

object LastTenTurnoverDisplayFormatter {

    fun format(
        transaction: TransactionResultDetail,
        typeTitle: String,
        topUpTypeTitle: String,
        cardPrefix: String,
        billIdLabel: String,
        payIdLabel: String,
        mobileLabel: String,
        destinationCardLabel: String,
        walletLabel: String,
        voucherSerialLabel: String,
        currencyLabel: String,
        operatorName: String,
        locale: AppLocale,
    ): LastTenTurnoverDisplay {
        val dateTimeLine = buildDateTimeLine(transaction, locale)
        val typeLine = buildTypeLine(
            transaction = transaction,
            typeTitle = typeTitle,
            topUpTypeTitle = topUpTypeTitle,
            operatorName = operatorName,
        )
        val detailLines = buildDetailLines(
            transaction = transaction,
            cardPrefix = cardPrefix,
            billIdLabel = billIdLabel,
            payIdLabel = payIdLabel,
            mobileLabel = mobileLabel,
            destinationCardLabel = destinationCardLabel,
            walletLabel = walletLabel,
            voucherSerialLabel = voucherSerialLabel,
            locale = locale,
        )
        val amountLine = buildAmountLine(transaction.amount, currencyLabel, locale)
        return LastTenTurnoverDisplay(
            dateTimeLine = dateTimeLine,
            typeLine = typeLine,
            detailLines = detailLines,
            amountLine = amountLine,
        )
    }

    private fun buildDateTimeLine(
        transaction: TransactionResultDetail,
        locale: AppLocale,
    ): String {
        val time = formatTurnoverTime(transaction.time, locale)
        val date = formatTurnoverShamsiDate(transaction.date, locale)
        return when {
            time.isNotBlank() && date.isNotBlank() -> "$time $date"
            time.isNotBlank() -> time
            date.isNotBlank() -> date
            else -> transaction.dateTime
        }
    }

    private fun buildTypeLine(
        transaction: TransactionResultDetail,
        typeTitle: String,
        topUpTypeTitle: String,
        operatorName: String,
    ): String = when (transaction.transactionType) {
        TransactionType.TOPUP -> {
            if (operatorName.isNotBlank()) {
                "$topUpTypeTitle ($operatorName)"
            } else {
                topUpTypeTitle
            }
        }
        else -> typeTitle
    }

    private fun buildDetailLines(
        transaction: TransactionResultDetail,
        cardPrefix: String,
        billIdLabel: String,
        payIdLabel: String,
        mobileLabel: String,
        destinationCardLabel: String,
        walletLabel: String,
        voucherSerialLabel: String,
        locale: AppLocale,
    ): List<LastTenTurnoverDetailLine> {
        val lines = mutableListOf<LastTenTurnoverDetailLine>()
        formatCardLine(transaction, cardPrefix, locale)?.let(lines::add)

        when (transaction.transactionType) {
            TransactionType.BILL -> {
                if (transaction.billId.isNotBlank()) {
                    lines += detailLine(billIdLabel, formatDigits(transaction.billId, locale))
                }
                if (transaction.payId.isNotBlank()) {
                    lines += detailLine(payIdLabel, formatDigits(transaction.payId, locale))
                }
            }

            TransactionType.TOPUP -> {
                if (transaction.mobileNumber.isNotBlank()) {
                    lines += detailLine(mobileLabel, formatDigits(transaction.mobileNumber, locale))
                }
            }

            TransactionType.CARD_TO_CARD -> {
                if (transaction.destinationPan.isNotBlank()) {
                    lines += detailLine(
                        label = destinationCardLabel,
                        value = formatCardNumber(transaction.destinationPan, locale),
                        valueLtr = true,
                    )
                }
            }

            TransactionType.CARD_TO_WALLET -> {
                if (transaction.walletCode.isNotBlank()) {
                    lines += detailLine(walletLabel, formatDigits(transaction.walletCode, locale))
                }
            }

            TransactionType.WALLET_TO_WALLET -> {
                if (transaction.destinationPan.isNotBlank()) {
                    lines += detailLine(
                        label = destinationCardLabel,
                        value = formatCardNumber(transaction.destinationPan, locale),
                        valueLtr = true,
                    )
                }
            }

            TransactionType.VOUCHER -> {
                if (transaction.mobileNumber.isNotBlank()) {
                    lines += detailLine(mobileLabel, formatDigits(transaction.mobileNumber, locale))
                }
                if (transaction.voucherSerial.isNotBlank()) {
                    lines += detailLine(
                        voucherSerialLabel,
                        formatDigits(transaction.voucherSerial, locale),
                    )
                }
            }

            else -> Unit
        }

        return lines
    }

    private fun detailLine(
        label: String,
        value: String,
        valueLtr: Boolean = false,
    ): LastTenTurnoverDetailLine = LastTenTurnoverDetailLine(
        label = label,
        value = value,
        valueLtr = valueLtr,
    )

    private fun formatCardLine(
        transaction: TransactionResultDetail,
        cardPrefix: String,
        locale: AppLocale,
    ): LastTenTurnoverDetailLine? {
        val pan = transaction.maskedPan.ifBlank { transaction.pan }.trim()
        if (pan.isBlank()) return null
        if (transaction.transactionType == TransactionType.CARD_TO_WALLET &&
            transaction.walletCode.isNotBlank() &&
            pan.filter(Char::isDigit).length < 10
        ) {
            return null
        }
        return detailLine(
            label = cardPrefix,
            value = formatCardNumber(pan, locale),
            valueLtr = true,
        )
    }

    fun formatTurnoverTime(timeHhMmSs: String, locale: AppLocale): String {
        val digits = timeHhMmSs.filter { it.isDigit() }
        if (digits.length < 4) return formatDigits(timeHhMmSs, locale)
        val hour = digits.take(2).trimStart('0').ifEmpty { "0" }
        val minute = digits.substring(2, 4)
        return formatDigits("$hour:$minute", locale)
    }

    fun formatTurnoverShamsiDate(dateYyyyMmDd: String, locale: AppLocale): String {
        val normalized = TransactionDateTimeFormatter.normalizeDateInput(dateYyyyMmDd) ?: return ""
        val shamsi = TransactionDateTimeFormatter.formatDate(
            dateYyyyMmDd = normalized,
            locale = locale,
            calendarStyle = ReceiptCalendarStyle.IRANIAN_SHAMSI,
        )
        val parts = shamsi.split('/')
        if (parts.size != 3) return shamsi
        return "${parts[2]} / ${parts[1]} / ${parts[0]}"
    }

    fun formatCardNumber(pan: String, locale: AppLocale): String {
        val digits = pan.filter { it.isDigit() }
        if (digits.length < 10) return formatDigits(pan, locale)
        val formatted = "${digits.take(4)} ** ${digits.takeLast(6)}"
        return formatDigits(formatted, locale)
    }

    fun formatTurnoverAmount(amountRaw: String, locale: AppLocale): String {
        val digits = amountRaw.filter { it.isDigit() }
        if (digits.isBlank()) return formatDigits(amountRaw, locale)
        val amount = digits.toLongOrNull() ?: return formatDigits(amountRaw, locale)
        val formatted = if (amount < 1_000) {
            ".${amount.toString().padStart(6, '0')}"
        } else {
            amount.toString()
                .reversed()
                .chunked(3)
                .joinToString(".")
                .reversed()
        }
        return formatDigits(formatted, locale)
    }

    private fun buildAmountLine(
        amountRaw: String,
        currencyLabel: String,
        locale: AppLocale,
    ): String {
        if (amountRaw.isBlank()) return "— $currencyLabel"
        val formattedAmount = formatTurnoverAmount(amountRaw, locale)
        return "$formattedAmount $currencyLabel"
    }

    private fun formatDigits(value: String, locale: AppLocale): String =
        if (locale == AppLocale.ENGLISH) value else TransactionDateTimeFormatter.toPersianDigits(value)
}
