package com.danesh.hp.balance

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
        request: HpBalanceRequest
    ): HpBalanceResult {

        return withContext(Dispatchers.IO){
           executor.execute(
                request = request,
                handler = handler
            )

        }
    }
}