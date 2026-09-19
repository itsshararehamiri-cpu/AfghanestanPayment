package com.danesh.sadad.transactionsummary

import com.danesh.api.TransactionRequest

/** 18-TRANSACTION SUMMARY (MTI 0100/0110، DE3 430000). */
data class SadadTransactionSummaryRequest(
    val pinBlock: String,
) : TransactionRequest
