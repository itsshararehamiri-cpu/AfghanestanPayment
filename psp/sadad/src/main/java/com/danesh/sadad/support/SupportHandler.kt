package com.danesh.sadad.support

import com.danesh.api.SupportCatalog
import com.danesh.api.SupportMenuItem
import com.danesh.api.SupportUserInput
import com.danesh.api.TransactionType
import com.danesh.sadad.util.SadadDetailHandler
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportHandler @Inject constructor(
    builder: SadadSupportMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadDetailHandler<SupportUserInput>(
    type = TransactionType.SUPPORT,
    isReversible = false,
    needReport = true,
    build = builder::build,
    messages = messages,
    transport = transport,
)

@Singleton
class EmptySupportCatalog @Inject constructor() : SupportCatalog {
    override fun items(): List<SupportMenuItem> = emptyList()
}
