package com.danesh.bp.balance

import android.util.Log
import com.danesh.engine.TransactionExecutor
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BalanceTransactionUseCase @Inject constructor(
    private val executor: TransactionExecutor<IsoMessage>,
    private val handler: BalanceHandler
) {

    suspend operator fun invoke(
        request: BpBalanceRequest
    ): BpBalanceResult {

        return withContext(Dispatchers.IO){
           executor.execute(
                request = request,
                handler = handler
            )

        }
    }
}