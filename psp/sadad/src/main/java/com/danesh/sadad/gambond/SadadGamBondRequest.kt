package com.danesh.sadad.gambond

import com.danesh.api.TransactionRequest

/** 22-GAM BOND (MTI 0200/0210، DE3 700000). */
data class SadadGamBondRequest(
    val track2: String,
    val pinBlock: String,
    val amount: String,
) : TransactionRequest
