package com.danesh.hp.transfer

import com.danesh.api.TransferFlowPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpTransferFlowPolicy @Inject constructor() : TransferFlowPolicy {
    override val requiresNameInquiry: Boolean = true
    override val supportsWalletTransfer: Boolean = true
}
