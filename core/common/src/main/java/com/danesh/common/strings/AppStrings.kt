package com.danesh.common.strings

import android.content.Context
import com.danesh.common.R
import com.danesh.api.TransactionTransportCodes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStrings @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun get(@androidx.annotation.StringRes resId: Int): String = context.getString(resId)

    fun txSuccess(): String = context.getString(R.string.tx_message_success)
    fun txFailed(): String = context.getString(R.string.tx_message_failed)
    fun txQueueFailed(): String = context.getString(R.string.tx_message_queue_failed)
    fun txConnectFailed(): String = context.getString(R.string.tx_message_connect_failed)
    fun txSendFailed(): String = context.getString(R.string.tx_message_send_failed)
    fun txReceiveFailed(): String = context.getString(R.string.tx_message_receive_failed)
    fun txNetworkFailed(): String = context.getString(R.string.tx_message_network_failed)
    fun txNotProvisioned(): String = context.getString(R.string.tx_message_not_provisioned)

    fun transportMessageFor(code: String): String = when (TransactionTransportCodes.normalizeCode(code)) {
        TransactionTransportCodes.QUEUE_BLOCKED -> txQueueFailed()
        TransactionTransportCodes.CONNECT_FAILED -> txConnectFailed()
        TransactionTransportCodes.SEND_FAILED -> txSendFailed()
        TransactionTransportCodes.RECEIVE_FAILED -> txReceiveFailed()
        TransactionTransportCodes.NETWORK_ERROR -> txNetworkFailed()
        else -> txFailed()
    }
    fun invalidCardNumber(): String = context.getString(R.string.error_invalid_card_number)
    fun wrongPassword(): String = context.getString(R.string.error_wrong_password)
    fun wrongCurrentPassword(): String = context.getString(R.string.error_current_password_wrong)
    fun invalidCard(): String = context.getString(R.string.error_invalid_card)
    fun noTransactionFound(): String = context.getString(R.string.error_transaction_not_found)
    fun reportLoadFailed(): String = context.getString(R.string.error_report_load_failed)
    fun balanceSuccessTitle(): String = context.getString(R.string.balance_success_title)
    fun balanceFailedTitle(): String = context.getString(R.string.balance_failed_title)
    fun purchaseSuccessTitle(): String = context.getString(R.string.purchase_success_title)
    fun purchaseFailedTitle(): String = context.getString(R.string.purchase_failed_title)
    fun terminalMerchantLabel(): String = context.getString(R.string.terminal_merchant_label)
    fun notImplementedCardToCard(): String = context.getString(R.string.card_to_card_not_implemented)
    fun notImplementedTopUp(): String = context.getString(R.string.topup_not_implemented)
    fun notImplementedBill(): String = context.getString(R.string.bill_not_implemented)
    fun validationEnterDestinationCard(): String = context.getString(R.string.validation_enter_destination_card)
    fun validationCard16Digits(): String = context.getString(R.string.validation_card_16_digits)
    fun validationWallet8Digits(): String = context.getString(R.string.validation_wallet_8_digits)
    fun validationEnterAmount(): String = context.getString(R.string.validation_enter_amount)
    fun validationEnterMobile(): String = context.getString(R.string.validation_enter_mobile)
    fun validationMobile10Digits(): String = context.getString(R.string.validation_mobile_10_digits)
    fun validationMobilePrefix(): String = context.getString(R.string.validation_mobile_prefix)
    fun validationSelectOperator(): String = context.getString(R.string.validation_select_operator)
    fun validationEnterBillId(): String = context.getString(R.string.validation_enter_bill_id)
    fun validationEnterPaymentId(): String = context.getString(R.string.validation_enter_payment_id)
    fun billTypeGeneric(): String = context.getString(R.string.bill_type_generic)
    fun billTypeWater(): String = context.getString(R.string.bill_type_water)
}
