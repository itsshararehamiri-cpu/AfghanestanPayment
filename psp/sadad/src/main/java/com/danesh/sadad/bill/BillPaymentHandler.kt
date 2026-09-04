package com.danesh.sadad.bill

import com.danesh.api.BillUserInput
import com.danesh.api.TransactionType
import com.danesh.sadad.util.SadadDetailHandler
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillPaymentHandler @Inject constructor(
    builder: SadadBillPaymentMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadDetailHandler<BillUserInput>(
    type = TransactionType.BILL,
    isReversible = true,
    needReport = true,
    build = builder::build,
    messages = messages,
    transport = transport,
)
