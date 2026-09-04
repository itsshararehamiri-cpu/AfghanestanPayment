package com.danesh.hp.queue

import com.danesh.api.AdviceReverseResult

class HpAdviceResult(
    isSuccess: Boolean = false,
    responseCode: String = "",
) : AdviceReverseResult(isSuccess, responseCode)
