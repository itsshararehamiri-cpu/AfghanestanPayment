package com.danesh.bp.queue

import com.danesh.api.AdviceReverseRequest
import com.danesh.api.QueueItem

class BpReverseRequest(
    override val queueItem: QueueItem,
) : AdviceReverseRequest(queueItem)
