package com.danesh.coupon.presentation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.danesh.api.CouponPurchaseInput
import com.danesh.api.PspGateway
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.common.card.CardSession
import com.danesh.common.pin.BaseGetPinViewModel
import com.danesh.coupon.data.CouponFlowSession
import com.danesh.core.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** خرید کالابرگ با رمز کارت؛ مبالغ و شماره پیگیری از پاسخ استعلام. */
@HiltViewModel
class CouponGetPinViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    device: Device,
    cardSession: CardSession,
    @ApplicationContext context: Context,
    private val session: CouponFlowSession,
    private val pspGateway: PspGateway,
) : BaseGetPinViewModel(savedStateHandle, device, cardSession, context) {

    override suspend fun executeTransaction(
        pinBlock: String,
        track2: String,
        pan: String,
    ): TransactionResultDetail {
        val inquiry = session.inquiry ?: return TransactionResultDetail(
            isSuccess = false,
            transactionType = TransactionType.COUPON_PURCHASE,
        )
        return pspGateway.couponPurchase(
            CouponPurchaseInput(
                track2 = track2,
                pinBlock = pinBlock,
                pan = pan,
                amount = inquiry.amount.filter(Char::isDigit).toLongOrNull() ?: 0L,
                creditAmount = inquiry.couponCreditRequired.toLongOrNull() ?: 0L,
                inquiryRrn = inquiry.rrn.orEmpty(),
                couponTrackingNumber = inquiry.couponTrackingNumber,
            ),
        )
    }
}
