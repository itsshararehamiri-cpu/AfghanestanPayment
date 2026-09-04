package com.danesh.hp.init

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.IsoResponseCodes
import com.danesh.api.PspDeviceOperations
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.key.HpKeyConfig
import com.danesh.hp.key.HpKeyMaterial
import com.danesh.hp.util.HpIsoHandlerSupport
import com.danesh.hp.util.HpTransactionMessages
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitHandler @Inject constructor(
    private val initMessageBuilder: HpInitMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
    private val configurationStore: DeviceConfigurationStore,
    private val deviceOperations: PspDeviceOperations,    private val contextProvider: TransactionContextProvider,

    ) : HandlerTransaction<HpInitRequest, HpInitResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: HpInitRequest): IsoMessage =
        initMessageBuilder.build()

    override fun queueFailure(request: HpInitRequest): HpInitResult {
        val message = buildMessage(request)
        return HpInitResult(
            detail = transport.map(
                transactionType = TransactionType.INIT,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed(),
                masterKey = "",
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: HpInitRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpInitResult = HpInitResult(
        detail = transport.map(
            transactionType = TransactionType.INIT,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
            masterKey = "",
        ),
    )

    override fun success(
        request: HpInitRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpInitResult {
        return try {
            val current = contextProvider.getTerminalConfig()
response?.getDump()
            response?.print("lllll>")
//            val updated = current.copy(
//                merchantName = decodedMerchantNameFa ?: current.merchantName,
//                merchantPhone = tagValue(response, BpField48Tags.MERCHANT_PHONE) ?: current.merchantPhone,
//                merchantAddress = tagValue(response, BpField48Tags.MERCHANT_ADDRESS) ?: current.merchantAddress,
//                englishMerchantName = decodedMerchantNameEn ?: current.englishMerchantName,
//                merchantPostalCode = tagValue(response, BpField48Tags.MERCHANT_POSTAL_CODE)
//                    ?: current.merchantPostalCode,
//            )
//            if (updated != current) {
//                contextProvider.saveTerminalConfig(updated)
//            }
            HpInitResult(
                detail = transport.map(
                    transactionType = TransactionType.INIT,
                    request = sentMessage,
                    response = response,
                    isSuccess = true,
                    responseMessage = messages.success(),
                    masterKey = HpKeyConfig.MASTER_KEY_HEX,
                ),
            )
        } catch (error: Exception) {
            HpInitResult(
                detail = transport.failureDetail(
                    transactionType = TransactionType.INIT,
                    sentMessage = sentMessage,
                    response = response,
                    responseCode = "96",
                    responseMessage = error.message.orEmpty().ifBlank { messages.failed() },
                ),
            )
        }
    }

    override fun connectFailure(
        request: HpInitRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpInitResult = HpInitResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.INIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpInitRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpInitResult = HpInitResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.INIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpInitRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpInitResult = HpInitResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.INIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpInitRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpInitResult = receiveFailure(request, sentMessage, e)
}
