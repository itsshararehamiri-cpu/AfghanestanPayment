package com.danesh.sadad.card_to_card

import com.danesh.api.CardToCardUserInput
import com.danesh.api.TransactionType
import com.danesh.sadad.util.SadadDetailHandler
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardToCardHandler @Inject constructor(
    builder: SadadCardToCardMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadDetailHandler<CardToCardUserInput>(
    type = TransactionType.CARD_TO_CARD,
    isReversible = true,
    needReport = true,
    needAdvice = true,
    build = builder::build,
    messages = messages,
    transport = transport,
)
