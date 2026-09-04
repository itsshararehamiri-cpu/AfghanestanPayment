package com.danesh.domain

import com.danesh.api.ResultWrapper
import com.danesh.model.BalanceRequest
import com.danesh.model.BalanceResult

interface BalanceTransactionUseCase  {
    suspend operator fun invoke(
        request: BalanceRequest
    ): ResultWrapper<BalanceResult>
}
//class BalanceTransactionUseCaseImpl @Inject constructor(
//    private val orchestrator: TransactionOrchestrator<IsoMessage, BalanceRequest, BalanceResult>,
//    private val pspFactory: PspTransactionFactory<IsoMessage>
//) : BalanceTransactionUseCase {
//    //    suspend operator fun invoke(request: BalanceRequest): ResultWrapper<BalanceResult> {
////        return orchestrator.execute(
////            request = request,
////            handler = pspFactory.balance(),
////            reversalHandler = pspFactory.adviceReverse()
////        )
////    }
////    override suspend fun invoke(request: FanavaBalanceRequest): ResultWrapper<FanavaBalanceResult> {
////        return orchestrator.execute(
////            request = request,
////            handler = pspFactory.balance(),
////            reversalHandler = pspFactory.adviceReverse()
////        )
////    }
//    override suspend fun invoke(request: BalanceRequest): ResultWrapper<BalanceResult> {
//        return orchestrator.execute(handler = pspFactory.balance(), request = request)
//    }
//}