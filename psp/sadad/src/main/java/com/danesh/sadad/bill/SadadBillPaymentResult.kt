package com.danesh.sadad.bill



import com.danesh.api.BillResult
import com.danesh.api.TransactionResultDetail

class SadadBillPaymentResult(
    val detail: TransactionResultDetail,
) : BillResult(detail.isSuccess)
