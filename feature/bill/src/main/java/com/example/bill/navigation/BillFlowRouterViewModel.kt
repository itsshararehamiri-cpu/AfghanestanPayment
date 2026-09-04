package com.example.bill.navigation

import androidx.lifecycle.ViewModel
import com.danesh.api.BillFlowPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BillFlowRouterViewModel @Inject constructor(
    billFlowPolicy: BillFlowPolicy,
) : ViewModel() {
    val requiresInquiry: Boolean = billFlowPolicy.requiresInquiry
}
