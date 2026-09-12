package com.danesh.hp.iso

import com.danesh.api.TransactionContextProvider
import com.danesh.iso.field48.HpField48Tlv
import javax.inject.Inject
import javax.inject.Singleton

/** متادیتای ترمینال HP — ساخت پیام ISO در builderهای اختصاصی هر تراکنش انجام می‌شود. */
@Singleton
class HpIsoMessageFactory @Inject constructor(
    private val contextProvider: TransactionContextProvider,
) {
    fun terminalMerchantId(): String = contextProvider.getTerminalConfig().merchantId

    fun terminalMerchantName(): String = contextProvider.getTerminalConfig().merchantName

    fun merchantPhone(): String = contextProvider.getTerminalConfig().merchantPhone

    /** تگ 030 پیکربندی فعال (DE72 پاسخ 1314) — طبق رفتار رسید، بعد از لوگو چاپ می‌شود. */
    fun receiptHeaderText(): String = configTag(TAG_RECEIPT_HEADER)

    /** تگ 031 پیکربندی فعال (DE72 پاسخ 1314) — طبق رفتار رسید، در انتهای رسید چاپ می‌شود. */
    fun receiptFooterText(): String = configTag(TAG_RECEIPT_FOOTER)

    private fun configTag(tag: String): String {
        val payload = contextProvider.getTerminalConfig().configPayload
        if (payload.isBlank()) return ""
        return HpField48Tlv().apply { unpack(payload) }.getNode(tag).orEmpty()
    }

    companion object {
        private const val TAG_RECEIPT_HEADER = "030"
        private const val TAG_RECEIPT_FOOTER = "031"
    }
}
