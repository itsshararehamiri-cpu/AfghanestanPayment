package com.danesh.hp.name_inquiry

import com.danesh.api.NameInquiryOutput
import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class HpNameInquiryResult(
    val inquiry: NameInquiryOutput,
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
