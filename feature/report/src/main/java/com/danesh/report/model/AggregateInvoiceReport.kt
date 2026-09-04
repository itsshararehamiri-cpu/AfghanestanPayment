package com.danesh.report.model

import com.danesh.api.TransactionType

data class AggregateServiceSummary(
    val type: TransactionType,
    val count: Int,
    val totalAmount: Long,
)

data class AggregateUnsettledRow(
    val time: String,
    val trace: String,
    val amount: Long,
)

data class AggregateUnsettledDayGroup(
    val type: TransactionType,
    val date: String,
    val rows: List<AggregateUnsettledRow>,
)

data class AggregateInvoiceReport(
    val terminalId: String,
    val reportDate: String,
    val fromDate: String,
    val toDate: String,
    val fromTime: String = "",
    val toTime: String = "",
    val currencyLabel: String,
    val services: List<AggregateServiceSummary>,
    val unsettledGroups: List<AggregateUnsettledDayGroup>,
    val totalAmountFormatted: String,
    val successfulCount: String,
)
