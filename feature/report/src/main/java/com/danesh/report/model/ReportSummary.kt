package com.danesh.report.model

data class ReportSummary(
    val totalAmountToday: String,
    val successfulTransactionsCount: String,
    val currency: String = "",
)

enum class ReportMenuType {
    AGGREGATE,
    TRANSACTION_DETAILS,
    LAST_TRANSACTION,
    UNSETTLED,
}
