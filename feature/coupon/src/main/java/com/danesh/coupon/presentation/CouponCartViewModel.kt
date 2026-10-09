package com.danesh.coupon.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.CouponCatalog
import com.danesh.api.CouponInquiryInput
import com.danesh.api.CouponProduct
import com.danesh.api.PspGateway
import com.danesh.api.TransactionResultDetail
import com.danesh.api.toJson
import com.danesh.coupon.R
import com.danesh.coupon.data.CouponCartLine
import com.danesh.coupon.data.CouponFlowSession
import com.danesh.coupon.data.CouponProductPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CouponCartUiState(
    val products: List<CouponProduct> = emptyList(),
    val lines: List<CouponCartLine> = emptyList(),
    /** شناسه ردیف‌هایی که مبلغشان وارد نشده (برای نمایش خطا). */
    val invalidLineIds: Set<Long> = emptySet(),
    val otherCount: Int = 0,
    val isLoading: Boolean = false,
    val message: String? = null,
) {
    val totalRials: Long get() = lines.sumOf { it.totalRials }
    val canAddOther: Boolean get() = otherCount < CouponCatalog.MAX_OTHER_ITEMS
}

sealed interface CouponCartEvent {
    /** استعلام موفق بود؛ [detail] در [CouponFlowSession.inquiry] هم هست. */
    data object InquirySucceeded : CouponCartEvent

    /** استعلام ناموفق؛ JSON نتیجه برای صفحه/رسید ناموفق. */
    data class InquiryFailed(val response: String) : CouponCartEvent
}

/**
 * انتخاب کالاهای کالابرگ و استعلام (فقط با کارت، بدون رمز).
 * «سایر» تا ۹ بار قابل افزودن است؛ کالای تعدادی (پوشک، شیرخشک، سایر) مبلغ × تعداد حساب می‌شود.
 */
@HiltViewModel
class CouponCartViewModel @Inject constructor(
    catalog: CouponCatalog,
    preferences: CouponProductPreferences,
    private val session: CouponFlowSession,
    private val pspGateway: PspGateway,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CouponCartUiState(
            products = catalog.products().filter { preferences.isEnabled(it.barcode) },
            lines = session.lines,
        ).withOtherCount(),
    )
    val uiState: StateFlow<CouponCartUiState> = _uiState.asStateFlow()

    private val _events = Channel<CouponCartEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var nextId = (session.lines.maxOfOrNull { it.id } ?: 0L) + 1

    fun addProduct(product: CouponProduct) {
        val state = _uiState.value
        if (product.isOther) {
            if (!state.canAddOther) {
                _uiState.update { it.copy(message = context.getString(R.string.coupon_other_limit, CouponCatalog.MAX_OTHER_ITEMS)) }
                return
            }
        } else if (state.lines.any { it.product.barcode == product.barcode }) {
            // کالای عادی فقط یک بار؛ به ردیف موجود اشاره می‌کنیم.
            _uiState.update { it.copy(message = context.getString(R.string.coupon_already_added)) }
            return
        }
        updateLines(state.lines + CouponCartLine(id = nextId++, product = product))
    }

    fun removeLine(id: Long) = updateLines(_uiState.value.lines.filterNot { it.id == id })

    fun onAmountChange(id: Long, value: String) {
        val digits = value.filter(Char::isDigit).take(12).trimStart('0')
        updateLines(_uiState.value.lines.map { if (it.id == id) it.copy(amountDigits = digits) else it })
        _uiState.update { it.copy(invalidLineIds = it.invalidLineIds - id) }
    }

    fun onCountChange(id: Long, count: Int) {
        updateLines(
            _uiState.value.lines.map {
                if (it.id == id) it.copy(count = count.coerceIn(1, MAX_COUNT)) else it
            },
        )
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun inquire() {
        val state = _uiState.value
        if (state.isLoading) return
        if (state.lines.isEmpty()) {
            _uiState.update { it.copy(message = context.getString(R.string.coupon_cart_empty)) }
            return
        }
        val invalid = state.lines.filter { it.amountRials <= 0 }.map { it.id }.toSet()
        if (invalid.isNotEmpty()) {
            _uiState.update {
                it.copy(invalidLineIds = invalid, message = context.getString(R.string.coupon_enter_amounts))
            }
            return
        }
        session.lines = state.lines
        _uiState.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            val result = runCatching {
                pspGateway.couponInquiry(
                    CouponInquiryInput(
                        track2 = session.track2,
                        pinBlock = "",
                        pan = session.pan,
                        items = state.lines.map { it.toOrderItem() },
                    ),
                )
            }.getOrElse { error ->
                TransactionResultDetail(
                    isSuccess = false,
                    transactionType = com.danesh.api.TransactionType.COUPON_INQUIRY,
                    responseMessage = error.message.orEmpty().ifBlank { context.getString(R.string.coupon_inquiry_failed) },
                )
            }
            _uiState.update { it.copy(isLoading = false) }
            if (result.isSuccess && result.couponTrackingNumber.isNotBlank()) {
                session.inquiry = result
                _events.send(CouponCartEvent.InquirySucceeded)
            } else {
                _events.send(CouponCartEvent.InquiryFailed(result.toJson()))
            }
        }
    }

    private fun updateLines(lines: List<CouponCartLine>) {
        session.lines = lines
        _uiState.update { it.copy(lines = lines).withOtherCount() }
    }

    private fun CouponCartUiState.withOtherCount(): CouponCartUiState =
        copy(otherCount = lines.count { it.product.isOther })

    private companion object {
        const val MAX_COUNT = 99
    }
}
