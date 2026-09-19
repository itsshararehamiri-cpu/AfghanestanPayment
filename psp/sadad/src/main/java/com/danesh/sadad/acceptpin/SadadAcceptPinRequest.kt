package com.danesh.sadad.acceptpin

import com.danesh.api.TransactionRequest

/** 19-ACCEPT PIN (MTI 0100/0110، DE3 710000). */
data class SadadAcceptPinRequest(
    val pinBlock: String,
) : TransactionRequest
