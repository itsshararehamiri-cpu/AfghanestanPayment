package com.danesh.sadad.fixedduty

import com.danesh.api.TransactionRequest

/** 11-FIXED DUTY (MTI 0200/0210، DE3 220000). */
data class SadadFixedDutyRequest(
    val track2: String,
    val pinBlock: String,
    val amount: String,
) : TransactionRequest
