package com.danesh.model

import com.danesh.api.TransactionRequest

open class BalanceRequest(val pinBlock: String,val track2: String): TransactionRequest
