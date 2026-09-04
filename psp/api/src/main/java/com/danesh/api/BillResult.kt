package com.danesh.api


open class BillResult(
    override val isSuccess: Boolean = false,
) : TransactionResult
