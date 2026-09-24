package com.danesh.sadad.keycard

import android.util.Log
import com.danesh.api.KeyCardKcvSummary
import com.danesh.api.KeyCardLoadingService
import com.danesh.api.KeyCardPinRejectedException
import com.danesh.api.KeyCardType
import com.danesh.sadad.key.SadadWorkingMacState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * پل بین انتزاع UI-agnostic [KeyCardLoadingService] (transaction:api) و پیاده‌سازی مشخص
 * سداد ([SadadKeyCardService])، تا feature:settings به ماژول psp:sadad وابسته نشود.
 */
@Singleton
class SadadKeyCardLoadingServiceAdapter @Inject constructor(
    private val service: SadadKeyCardService,
    private val workingMacState: SadadWorkingMacState,
) : KeyCardLoadingService {

    override suspend fun hasStoredKeyPair(keyIndex: Int): Boolean =
        service.hasStoredKeyPair(keyIndex)

    override suspend fun loadKeyPairFromCardA(pin: String, keyIndex: Int): Result<Unit> =
        service.loadKeyPairFromCardA(pin, keyIndex).translatePinError().onSuccess {
            workingMacState.saveRsaKeyIndex(keyIndex)
        }

    override fun persistedRsaKeyIndex(): Int? = workingMacState.rsaKeyIndex()

    override fun persistedCardCIndex(): Int? = workingMacState.persistedCardCIndex()

    override suspend fun loadAndInjectMasterKeys(
        card: KeyCardType,
        pin: String,
        keyIndex: Int,
        rsaKeyIndex: Int,
    ): Result<KeyCardKcvSummary> {
        require(card != KeyCardType.CARD_A) { "این عملیات فقط برای کارت B یا C است" }
        val sadadCard = when (card) {
            KeyCardType.CARD_B -> SadadKeyCard.CARD_B
            KeyCardType.CARD_C -> SadadKeyCard.CARD_C
            KeyCardType.CARD_A -> error("unreachable")
        }
        Log.d("TAG", "loadAndInjectMasterKeys: $sadadCard c=$keyIndex a=$rsaKeyIndex")
        return service.loadAndInjectMasterKeys(sadadCard, pin, keyIndex, rsaKeyIndex)
            .map { summary ->
                KeyCardKcvSummary(
                    terminalMasterKey = summary.terminalMasterKey,
                    mac = summary.mac,
                    data = summary.data,
                    pin = summary.pin,
                )
            }
            .translatePinError()
    }

    private fun <T> Result<T>.translatePinError(): Result<T> {
        val error = exceptionOrNull() ?: return this
        return if (error is SadadPinRejectedException) {
            Result.failure(KeyCardPinRejectedException(error.remainingTries))
        } else {
            this
        }
    }
}
