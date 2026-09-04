package com.danesh.report.model

import androidx.annotation.StringRes
import com.danesh.report.R

enum class TransactionStatus(@StringRes val labelRes: Int) {
    ALL(R.string.report_status_all),
    SUCCESS(R.string.report_status_success),
    FAILED(R.string.report_status_failed),
    PENDING(R.string.report_status_pending),
}

data class ReportFilterState(
    val trackingNumber: String = "",
    val referenceNumber: String = "",
    val transactionStatus: TransactionStatus? = null,
    val fromDate: String = "",
    val toDate: String = "",
    val fromTime: String = "",
    val toTime: String = "",
    val fromAmount: String = "",
    val toAmount: String = "",
)
