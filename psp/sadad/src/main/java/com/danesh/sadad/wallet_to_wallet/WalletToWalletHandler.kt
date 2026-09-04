package com.danesh.sadad.wallet_to_wallet

import com.danesh.api.TransactionType
import com.danesh.api.WalletToWalletUserInput
import com.danesh.sadad.util.SadadDetailHandler
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletToWalletHandler @Inject constructor(
    builder: SadadWalletToWalletMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadDetailHandler<WalletToWalletUserInput>(
    type = TransactionType.WALLET_TO_WALLET,
    isReversible = true,
    needReport = true,
    needAdvice = true,
    build = builder::build,
    messages = messages,
    transport = transport,
)
