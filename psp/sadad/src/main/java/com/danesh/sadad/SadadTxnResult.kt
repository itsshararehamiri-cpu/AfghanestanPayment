package com.danesh.sadad

import com.danesh.api.BillInquiryOutput
import com.danesh.api.NameInquiryOutput
import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResult
import com.danesh.api.TransactionResultDetail

class SadadTxnResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)

class SadadNetworkResult(
    val detail: TransactionResultDetail,
    /** Host FC 033 پاسخ (کف/سقف رسید اختیاری اعمال‌شده توسط سوئیچ)، اگر آمده باشد. */
    val optionalReceipt: com.danesh.api.OptionalReceiptLimits? = null,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}

class SadadBillInquiryResult(
    val inquiry: BillInquiryOutput? = null,
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)

class SadadNameInquiryResult(
    val inquiry: NameInquiryOutput,
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
