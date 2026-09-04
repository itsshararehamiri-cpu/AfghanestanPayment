package com.danesh.sadad.topup

import com.danesh.api.TopUpUserInput
import com.danesh.api.TransactionType
import com.danesh.sadad.util.SadadDetailHandler
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopUpHandler @Inject constructor(
    builder: SadadTopUpMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadDetailHandler<TopUpUserInput>(
    type = TransactionType.TOPUP,
    isReversible = true,
    needReport = true,
    build = builder::build,
    messages = messages,
    transport = transport,
)
