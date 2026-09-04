package com.danesh.report.receipt

data class ReportTablePrintLabels(
    val title: String,
    val typeColumn: String,
    val dateColumn: String,
    val timeColumn: String,
    val amountColumn: String,
    val referenceColumn: String,
    val emptyMessage: String,
    val printSuccessMessage: String,
    val printFailedMessage: String,
)

data class ReportTableRow(
    val type: String,
    val date: String,
    val time: String,
    val amount: String,
    val reference: String,
)
