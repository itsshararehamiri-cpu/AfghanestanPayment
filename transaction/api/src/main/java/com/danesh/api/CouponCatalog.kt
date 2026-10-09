package com.danesh.api

/**
 * کالای کالابرگ از فهرست PSP (مثلاً MENUITEM با SERVICE="GOODBASKET" در سداد).
 *
 * @param barcode شناسه کالا (SERVICEIDENTIFIER)
 * @param countable کالای «تعدادی» (پوشک، شیرخشک، سایر): مبلغ واردشده در تعداد ضرب می‌شود
 */
data class CouponProduct(
    val barcode: String,
    val nameFa: String,
    val nameEn: String,
    val countable: Boolean,
    /** کد واحد کالا که در استعلام ارسال می‌شود. */
    val unitCode: String,
    /** «سایر»: تا [CouponCatalog.MAX_OTHER_ITEMS] بار قابل افزودن است. */
    val isOther: Boolean = false,
) {
    fun displayName(english: Boolean): String =
        if (english) nameEn.ifBlank { nameFa } else nameFa.ifBlank { nameEn }
}

/** فهرست کالاهای کالابرگ PSP فعال (خالی یعنی PSP کالابرگ ندارد). */
interface CouponCatalog {
    fun products(): List<CouponProduct>

    companion object {
        /** کد کالای «سایر» در سداد. */
        const val OTHER_BARCODE = "1111111111111"
        const val MAX_OTHER_ITEMS = 9
    }
}
