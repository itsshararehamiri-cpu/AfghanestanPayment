package com.danesh.hp.bill

import com.danesh.api.BillInquiryOutput
import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class HpBillInquiryResult(
    val inquiry: BillInquiryOutput?=null,
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
