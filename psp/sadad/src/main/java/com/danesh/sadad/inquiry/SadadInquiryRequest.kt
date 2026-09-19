package com.danesh.sadad.inquiry

import com.danesh.api.TransactionRequest

/** 10-INQUIRY: استعلام چندپذیرنده‌ای (MTI 0100/0110، DE3 240000). */
data class SadadInquiryRequest(
    val track2: String,
    val amount: String,
) : TransactionRequest
