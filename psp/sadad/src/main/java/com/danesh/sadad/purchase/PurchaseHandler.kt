package com.danesh.sadad.purchase

import com.danesh.api.PurchaseUserInput
import com.danesh.api.TransactionType
import com.danesh.sadad.util.SadadDetailHandler
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseHandler @Inject constructor(
    builder: SadadPurchaseMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadDetailHandler<PurchaseUserInput>(
    type = TransactionType.PURCHASE,
    isReversible = true,
    needReport = true,
    build = builder::build,
    messages = messages,
    transport = transport,
)
