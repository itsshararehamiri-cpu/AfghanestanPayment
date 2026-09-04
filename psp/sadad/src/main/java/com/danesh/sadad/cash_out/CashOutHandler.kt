package com.danesh.sadad.cash_out

import com.danesh.api.CashOutUserInput
import com.danesh.api.TransactionType
import com.danesh.sadad.util.SadadDetailHandler
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashOutHandler @Inject constructor(
    builder: SadadCashOutMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadDetailHandler<CashOutUserInput>(
    type = TransactionType.CASH_OUT,
    isReversible = true,
    needReport = true,
    build = builder::build,
    messages = messages,
    transport = transport,
)
