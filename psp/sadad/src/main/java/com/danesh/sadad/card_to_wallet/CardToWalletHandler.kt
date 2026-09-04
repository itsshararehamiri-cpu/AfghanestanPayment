package com.danesh.sadad.card_to_wallet

import com.danesh.api.CardToWalletUserInput
import com.danesh.api.TransactionType
import com.danesh.sadad.util.SadadDetailHandler
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardToWalletHandler @Inject constructor(
    builder: SadadCardToWalletMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadDetailHandler<CardToWalletUserInput>(
    type = TransactionType.CARD_TO_WALLET,
    isReversible = true,
    needReport = true,
    needAdvice = true,
    build = builder::build,
    messages = messages,
    transport = transport,
)
