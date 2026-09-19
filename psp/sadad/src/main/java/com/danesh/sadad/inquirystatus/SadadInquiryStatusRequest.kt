package com.danesh.sadad.inquirystatus

import com.danesh.api.TransactionRequest

/** 13-INQUIRY STATUS (MTI 0100/0110، DE3 330000) — استعلام وضعیت روز/ماه مشخص. */
data class SadadInquiryStatusRequest(
    val month: String,
    val day: String,
) : TransactionRequest
