package com.danesh.sadad.queue

import com.danesh.api.AdviceReverseRequest
import com.danesh.api.AdviceReverseResult
import com.danesh.api.QueueItem

class SadadAdviceRequest(
    queueItem: QueueItem,
) : AdviceReverseRequest(queueItem)

class SadadReverseRequest(
    queueItem: QueueItem,
) : AdviceReverseRequest(queueItem)

class SadadAdviceResult(
    isSuccess: Boolean = false,
    responseCode: String = "",
) : AdviceReverseResult(isSuccess, responseCode)
