package com.danesh.coupon.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.CouponCancelInput
import com.danesh.api.PspGateway
import com.danesh.coupon.R
import com.danesh.coupon.data.CouponCartLine
import com.danesh.coupon.data.CouponFlowSession
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CouponInquiryResultUiState(
    val lines: List<CouponCartLine> = emptyList(),
    val totalAmount: Long = 0,
    val creditAmount: Long = 0,
    val cashAmount: Long = 0,
    val trackingNumber: String = "",
    val isCancelling: Boolean = false,
    /** پیام پایان لغو («استعلام لغو شد» یا خطا)؛ بعد از تأیید به منو برمی‌گردد. */
    val cancelMessage: String? = null,
)

/** نمایش نتیجهٔ استعلام کالابرگ: پرداخت (با رمز) یا لغو استعلام. */
@HiltViewModel
class CouponInquiryResultViewModel @Inject constructor(
    private val session: CouponFlowSession,
    private val pspGateway: PspGateway,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(load())
    val uiState: StateFlow<CouponInquiryResultUiState> = _uiState.asStateFlow()

    fun cancelInquiry() {
        val state = _uiState.value
        if (state.isCancelling || state.cancelMessage != null) return
        _uiState.update { it.copy(isCancelling = true) }
        viewModelScope.launch {
            val result = runCatching {
                pspGateway.couponCancel(
                    CouponCancelInput(
                        track2 = session.track2,
                        pan = session.pan,
                        couponTrackingNumber = state.trackingNumber,
                    ),
                )
            }.getOrNull()
            val message = if (result?.isSuccess == true) {
                context.getString(R.string.coupon_inquiry_cancelled)
            } else {
                context.getString(
                    R.string.coupon_inquiry_cancel_failed,
                    result?.responseMessage.orEmpty(),
                ).trim()
            }
            _uiState.update { it.copy(isCancelling = false, cancelMessage = message) }
        }
    }

    private fun load(): CouponInquiryResultUiState {
        val inquiry = session.inquiry
        val total = inquiry?.amount?.filter(Char::isDigit)?.toLongOrNull() ?: 0L
        val credit = inquiry?.couponCreditRequired?.toLongOrNull() ?: 0L
        return CouponInquiryResultUiState(
            lines = session.lines,
            totalAmount = total,
            creditAmount = credit,
            cashAmount = inquiry?.couponCashAmount?.toLongOrNull() ?: (total - credit).coerceAtLeast(0),
            trackingNumber = inquiry?.couponTrackingNumber.orEmpty(),
        )
    }
}
