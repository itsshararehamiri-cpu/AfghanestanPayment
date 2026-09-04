package com.danesh.report.data

import com.danesh.database.entity.TransactionReportEntity

internal object ReportReprintLookup {

    private const val STAN_LENGTH = 6
    private const val RRN_LENGTH = 12

    fun findEntity(
        entities: List<TransactionReportEntity>,
        trackingNumber: String,
        referenceNumber: String,
    ): TransactionReportEntity? {
        val stanQuery = trackingNumber.trim()
            .takeIf { it.isNotBlank() && !isDefaultTrace(it) }
        val rrnQuery = referenceNumber.trim()
            .takeIf { it.isNotBlank() && !isDefaultReference(it) }
        if (stanQuery == null && rrnQuery == null) return null

        return entities
            .asSequence()
            .filter { entity ->
                val stanOk = stanQuery == null || stanMatches(entity.stan, stanQuery)
                val rrnOk = rrnQuery == null || rrnMatches(entity.rrn.orEmpty(), rrnQuery)
                stanOk && rrnOk
            }
         //   .sortedNewestFirst()
            .firstOrNull()
    }

    fun stanMatches(stored: String, query: String): Boolean =
        matchesReferenceDigits(stored, query, STAN_LENGTH)

    fun rrnMatches(stored: String, query: String): Boolean =
        matchesReferenceDigits(stored, query, RRN_LENGTH)

    fun normalizeStan(value: String): String =
        value.filter(Char::isDigit).padStart(STAN_LENGTH, '0').takeLast(STAN_LENGTH)

    fun normalizeRrn(value: String): String =
        value.filter(Char::isDigit).padStart(RRN_LENGTH, '0').takeLast(RRN_LENGTH)

    private fun matchesReferenceDigits(stored: String, query: String, length: Int): Boolean {
        val storedDigits = stored.filter(Char::isDigit)
        val queryDigits = query.filter(Char::isDigit)
        if (queryDigits.isBlank()) return true
        if (storedDigits.isBlank()) return false

        val storedNorm = storedDigits.padStart(length, '0').takeLast(length)
        val queryNorm = queryDigits.padStart(length, '0').takeLast(length)
        if (storedNorm == queryNorm) return true

        val storedTrimmed = storedDigits.trimStart('0')
        val queryTrimmed = queryDigits.trimStart('0')
        if (queryTrimmed.isNotBlank() && storedTrimmed.contains(queryTrimmed)) return true
        if (storedDigits.contains(queryDigits)) return true
        return false
    }

    private fun isDefaultTrace(value: String): Boolean =
        value.filter(Char::isDigit).all { it == '0' }

    private fun isDefaultReference(value: String): Boolean =
        value.filter(Char::isDigit).all { it == '0' }
}
