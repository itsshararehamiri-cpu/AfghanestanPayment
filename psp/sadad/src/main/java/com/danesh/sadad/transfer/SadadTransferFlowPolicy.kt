package com.danesh.sadad.transfer

import com.danesh.api.TransferFlowPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadTransferFlowPolicy @Inject constructor() : TransferFlowPolicy {
    override val requiresNameInquiry: Boolean = true
    override val supportsWalletTransfer: Boolean = true
}
