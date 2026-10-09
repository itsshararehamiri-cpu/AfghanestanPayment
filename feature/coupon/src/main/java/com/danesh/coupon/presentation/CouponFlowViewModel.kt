package com.danesh.coupon.presentation

import androidx.lifecycle.ViewModel
import com.danesh.coupon.data.CouponFlowSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** دسترسی nav graph به وضعیت جریان کالابرگ (کارت کشیده‌شده). */
@HiltViewModel
class CouponFlowViewModel @Inject constructor(
    private val session: CouponFlowSession,
) : ViewModel() {
    val track2: String get() = session.track2
    val pan: String get() = session.pan

    fun start() = session.clear()

    fun onCardRead(track2: String, pan: String) = session.setCard(track2, pan)

    fun finish() = session.clear()
}
