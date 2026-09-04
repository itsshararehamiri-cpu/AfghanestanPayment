package com.danesh.sadad.balance

import com.danesh.api.BalanceUserInput
import com.danesh.api.TransactionType
import com.danesh.sadad.util.SadadDetailHandler
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceHandler @Inject constructor(
    builder: SadadBalanceMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadDetailHandler<BalanceUserInput>(
    type = TransactionType.BALANCE,
    isReversible = false,
    needReport = false,
    build = builder::build,
    messages = messages,
    transport = transport,
)
