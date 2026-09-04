package com.danesh.bp.purchase

import com.danesh.engine.TransactionExecutor
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PurchaseTransactionUseCase @Inject constructor(
    private val executor: TransactionExecutor<IsoMessage>,
    private val handler: PurchaseHandler
) {

    suspend operator fun invoke(
        request: BpPurchaseRequest
    ): BpPurchaseResult {

        return withContext(Dispatchers.IO){
            executor.execute(
                request = request,
                handler = handler
            )
        }
    }
}