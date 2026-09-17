package com.danesh.api



open class TopUpResult(
    override val isSuccess: Boolean = false,
    open val responseCode: String = "",
    open val responseMessage: String = "",
) : TransactionResult
