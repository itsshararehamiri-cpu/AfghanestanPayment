package com.danesh.bp.transfer

import com.danesh.api.TransferFlowPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpTransferFlowPolicy @Inject constructor() : TransferFlowPolicy {
    override val requiresNameInquiry: Boolean = false
    override val supportsWalletTransfer: Boolean = false
}
