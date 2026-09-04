package com.danesh.report.data

import com.danesh.api.TransactionType
import com.danesh.api.toTransactionType
import com.danesh.database.entity.TransactionReportEntity
import com.danesh.report.model.ReportFilterState
import com.danesh.report.model.ReportTransactionChipFilter
import com.danesh.report.model.TransactionStatus

internal object ReportFilterMatcher {

    fun matches(entity: TransactionReportEntity, filters: ReportFilterState): Boolean {
        if (!matchesText(entity.stan, filters.trackingNumber)) return false
        if (!matchesText(entity.rrn.orEmpty(), filters.referenceNumber)) return false
        if (!matchesDateTimeRange(
                date = entity.dateTransaction,
                time = entity.timeTransaction,
                fromDate = filters.fromDate,
                fromTime = filters.fromTime,
                toDate = filters.toDate,
                toTime = filters.toTime,
            )
        ) {
            return false
        }
        if (!matchesAmountRange(entity.amount, filters.fromAmount, filters.toAmount)) return false
        if (!matchesStatus(entity, filters.transactionStatus)) return false
        return true
    }

    fun matchesChip(entity: TransactionReportEntity, chip: ReportTransactionChipFilter): Boolean {
        val type = entity.resolveTransactionType()
        return when (chip) {
            ReportTransactionChipFilter.ALL -> true
            ReportTransactionChipFilter.PURCHASE ->
                type == TransactionType.PURCHASE && !entity.isLikelyTopUp()
            ReportTransactionChipFilter.TRANSFER ->
                type == TransactionType.CARD_TO_CARD ||
                    type == TransactionType.CARD_TO_WALLET ||
                    type == TransactionType.WALLET_TO_WALLET
            ReportTransactionChipFilter.BILL -> type == TransactionType.BILL || entity.isLikelyBill()
            ReportTransactionChipFilter.VOUCHER -> type == TransactionType.VOUCHER
            ReportTransactionChipFilter.TOPUP ->
                type == TransactionType.TOPUP || entity.isLikelyTopUp()
            ReportTransactionChipFilter.CASH_DEPOSIT -> type == TransactionType.CASH_DEPOSIT
            ReportTransactionChipFilter.CASH_OUT -> type == TransactionType.CASH_OUT
            ReportTransactionChipFilter.BALANCE -> type == TransactionType.BALANCE
            ReportTransactionChipFilter.SUPPORT -> type == TransactionType.SUPPORT
        }
    }

    private fun matchesText(value: String, filter: String): Boolean {
        val query = filter.trim()
        if (query.isBlank()) return true

        val normalizedValue = digitsOnly(value)
        val normalizedQuery = digitsOnly(query)
        if (normalizedQuery.isNotBlank()) {
            if (normalizedValue.contains(normalizedQuery)) return true
            if (normalizedValue.trimStart('0').contains(normalizedQuery.trimStart('0'))) return true
        }

        return value.contains(query, ignoreCase = true)
    }

    private fun matchesDateTimeRange(
        date: String,
        time: String,
        fromDate: String,
        fromTime: String,
        toDate: String,
        toTime: String,
    ): Boolean {
        val hasDateFilter = fromDate.isNotBlank() || toDate.isNotBlank()
        val hasTimeFilter = fromTime.isNotBlank() || toTime.isNotBlank()
        if (!hasDateFilter && !hasTimeFilter) return true

        if (hasDateFilter) {
            val entityKey = buildDateTimeKey(date, time) ?: return false
            val fromKey = buildBoundaryKey(fromDate, fromTime, isUpperBound = false)
            val toKey = buildBoundaryKey(toDate, toTime, isUpperBound = true)
            if (fromKey != null && entityKey < fromKey) return false
            if (toKey != null && entityKey > toKey) return false
            return true
        }

        return matchesTimeRange(time, fromTime, toTime)
    }

    private fun buildDateTimeKey(date: String, time: String): Long? {
        val normalizedDate = normalizeDate(date)
        if (normalizedDate.length < 8) return null
        val normalizedTime = normalizeTimeToHhMmSs(time)
        return "$normalizedDate$normalizedTime".toLongOrNull()
    }

    private fun buildBoundaryKey(date: String, time: String, isUpperBound: Boolean): Long? {
        val normalizedDate = normalizeDate(date)
        if (normalizedDate.length < 8) return null
        val normalizedTime = when {
            time.isNotBlank() -> normalizeTimeToHhMmSs(time)
            isUpperBound -> "235959"
            else -> "000000"
        }
        return "$normalizedDate$normalizedTime".toLongOrNull()
    }

    private fun normalizeDate(value: String): String {
        val digits = digitsOnly(value)
        return when {
            digits.length >= 8 -> digits.take(8)
            else -> digits
        }
    }

    private fun normalizeTimeToHhMmSs(value: String): String {
        val digits = digitsOnly(value)
        return when {
            digits.length >= 6 -> digits.take(6)
            digits.length >= 4 -> digits.take(4) + "00"
            else -> "000000"
        }
    }

    private fun matchesTimeRange(entityTime: String, fromTime: String, toTime: String): Boolean {
        val entityMinutes = parseTimeToMinutes(entityTime) ?: return fromTime.isBlank() && toTime.isBlank()
        val fromMinutes = parseTimeToMinutes(fromTime)
        val toMinutes = parseTimeToMinutes(toTime)
        if (fromMinutes != null && entityMinutes < fromMinutes) return false
        if (toMinutes != null && entityMinutes > toMinutes) return false
        return true
    }

    private fun parseTimeToMinutes(value: String): Int? {
        val digits = digitsOnly(value)
        if (digits.length < 4) return null
        val hour = digits.take(2).toIntOrNull() ?: return null
        val minute = digits.substring(2, 4).toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour * 60 + minute
    }

    private fun matchesAmountRange(amount: Long, fromAmount: String, toAmount: String): Boolean {
        val from = parseAmount(fromAmount)
        val to = parseAmount(toAmount)
        if (from != null && amount < from) return false
        if (to != null && amount > to) return false
        return true
    }

    private fun parseAmount(value: String): Long? {
        val digits = digitsOnly(value)
        if (digits.isBlank()) return null
        return digits.toLongOrNull()
    }

    private fun matchesStatus(entity: TransactionReportEntity, status: TransactionStatus?): Boolean {
        when (status) {
            null, TransactionStatus.ALL -> return true
            TransactionStatus.SUCCESS -> return entity.isSuccessful()
            TransactionStatus.FAILED -> return !entity.isSuccessful()
            TransactionStatus.PENDING -> return entity.responseCode == null
        }
    }

    private fun TransactionReportEntity.isSuccessful(): Boolean = responseCode == 0

    private fun TransactionReportEntity.isLikelyBill(): Boolean =
        !billId.isNullOrBlank() || !payId.isNullOrBlank()

    private fun TransactionReportEntity.isLikelyTopUp(): Boolean =
        !mobileNumber.isNullOrBlank() || operatorCode != null

    fun TransactionReportEntity.resolveTransactionType(): TransactionType {
        if (isLikelyBill()) return TransactionType.BILL
        return when (processingCode) {
            "310000" -> TransactionType.BALANCE
            "210000" -> TransactionType.CASH_DEPOSIT
            "010000" -> TransactionType.CASH_OUT
            "000000" -> TransactionType.PURCHASE
            "100000" -> TransactionType.SUPPORT
            "150000" -> TransactionType.VOUCHER
            "230000" -> TransactionType.TOPUP
            "170000" -> TransactionType.BILL
            else -> type.toTransactionType()
        }
    }

    private fun digitsOnly(value: String): String = buildString(value.length) {
        value.forEach { char ->
            append(
                when (char) {
                    in '0'..'9' -> char
                    in '۰'..'۹' -> ('0' + (char.code - '۰'.code)).toChar()
                    in '٠'..'٩' -> ('0' + (char.code - '٠'.code)).toChar()
                    else -> ""
                },
            )
        }
    }
}

internal fun ReportFilterState.normalizedForQuery(): ReportFilterState {
    val trimmed = copy(
        trackingNumber = trackingNumber.trim(),
        referenceNumber = referenceNumber.trim(),
        fromDate = fromDate.trim(),
        toDate = toDate.trim(),
        fromTime = fromTime.trim(),
        toTime = toTime.trim(),
        fromAmount = fromAmount.trim(),
        toAmount = toAmount.trim(),
        transactionStatus = transactionStatus.takeUnless { it == TransactionStatus.ALL },
    )
    return trimmed.withOrderedRanges()
}

private fun ReportFilterState.withOrderedRanges(): ReportFilterState {
    var state = this

    val fromDateKey = state.fromDate.filter(Char::isDigit).take(8)
    val toDateKey = state.toDate.filter(Char::isDigit).take(8)
    if (fromDateKey.length == 8 && toDateKey.length == 8 && fromDateKey > toDateKey) {
        state = state.copy(fromDate = toDate, toDate = fromDate)
    }

    val fromMinutes = parseFilterTimeMinutes(state.fromTime)
    val toMinutes = parseFilterTimeMinutes(state.toTime)
    if (fromMinutes != null && toMinutes != null && fromMinutes > toMinutes) {
        state = state.copy(fromTime = toTime, toTime = fromTime)
    }

    val fromAmountValue = parseFilterAmount(state.fromAmount)
    val toAmountValue = parseFilterAmount(state.toAmount)
    if (fromAmountValue != null && toAmountValue != null && fromAmountValue > toAmountValue) {
        state = state.copy(fromAmount = toAmount, toAmount = fromAmount)
    }

    return state
}

private fun parseFilterTimeMinutes(value: String): Int? {
    val digits = value.filter { it.isDigit() }
    if (digits.length < 4) return null
    val hour = digits.take(2).toIntOrNull() ?: return null
    val minute = digits.substring(2, 4).toIntOrNull() ?: return null
    return hour * 60 + minute
}

private fun parseFilterAmount(value: String): Long? {
    val digits = value.filter { it.isDigit() }
    if (digits.isBlank()) return null
    return digits.toLongOrNull()
}
