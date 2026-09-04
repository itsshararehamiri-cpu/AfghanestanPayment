package com.danesh.api

open class AdviceReverseResult(
    override val isSuccess: Boolean = false,
    open val responseCode: String = "",
) : TransactionResult
