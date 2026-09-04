package com.danesh.sadad.voucher

import com.danesh.api.TransactionType
import com.danesh.api.VoucherUserInput
import com.danesh.sadad.util.SadadDetailHandler
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherHandler @Inject constructor(
    builder: SadadVoucherMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadDetailHandler<VoucherUserInput>(
    type = TransactionType.VOUCHER,
    isReversible = true,
    needReport = true,
    build = builder::build,
    messages = messages,
    transport = transport,
)
