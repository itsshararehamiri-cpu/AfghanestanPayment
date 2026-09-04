package com.danesh.bp.bill


import android.util.Log
import com.danesh.engine.TransactionExecutor
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BillTransactionUseCase @Inject constructor(
    private val executor: TransactionExecutor<IsoMessage>,
    private val handler: BillHandler
) {

    suspend operator fun invoke(
        request: BpBillRequest
    ): BpBillResult {

        return withContext(Dispatchers.IO){
            executor.execute(
                request = request,
                handler = handler
            )

        }
    }
}