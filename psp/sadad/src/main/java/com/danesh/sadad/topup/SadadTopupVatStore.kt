package com.danesh.sadad.topup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * درصد مالیات شارژ مستقیم که سوئیچ در Host Function Code 018 اعلام می‌کند — با همان دقت اعشار.
 * تا وقتی سوئیچ مقداری نفرستاده، `null` است و مقدار ChargeList (taxPercent) استفاده می‌شود.
 */
@Singleton
class SadadTopupVatStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): BigDecimal? = prefs.getString(KEY_PERCENT, null)
        ?.let { runCatching { BigDecimal(it) }.getOrNull() }

    fun save(percent: BigDecimal) {
        prefs.edit().putString(KEY_PERCENT, percent.toPlainString()).apply()
    }

    private companion object {
        const val PREFS_NAME = "sadad_topup_vat"
        const val KEY_PERCENT = "percent"
    }
}

object SadadTopupAmounts {
    /** مبلغ قابل پرداخت = مبلغ شارژ + مالیات (گرد شده به نزدیک‌ترین ریال). */
    fun payableWithTax(charge: Long, taxPercent: BigDecimal?): Long {
        if (taxPercent == null || taxPercent.signum() <= 0) return charge
        val tax = BigDecimal(charge).multiply(taxPercent).divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
        return charge + tax.toLong()
    }
}
