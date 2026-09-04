package com.danesh.balance.domain


import android.util.Log
import com.danesh.api.BalanceInput
import com.danesh.api.BalanceOutput
import com.danesh.api.PspGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BalanceUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(pinBlock: String, track2: String,pan: String): BalanceOutput {
        Log.d("BalanceFlow", "BalanceUseCase | invoke | panLen=${pan.length}")
        return withContext(Dispatchers.IO) {
            val result = pspGateway.balance(
                BalanceInput(
                    track2 = track2,
                    pinBlock = pinBlock,pan=pan
                ),
            )
            Log.d("BalanceFlow", "BalanceUseCase | result | success=${result.isSuccess} | code=${result.responseCode} | msg=${result.responseMessage}")
            result
        }
    }
}
