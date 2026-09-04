package com.danesh.bp.queue

import com.danesh.api.AdviceReverseResult

class BpAdviceResult(
    isSuccess: Boolean = false,
    responseCode: String = "",
) : AdviceReverseResult(isSuccess, responseCode)
