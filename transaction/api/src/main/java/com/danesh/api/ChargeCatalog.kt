package com.danesh.api

enum class ChargeKind {
    VOUCHER,
    TOPUP,
}

data class ChargeOperator(
    val providerId: String,
    val nameFa: String,
    val nameEn: String,
    val ussdChargeCommand: String,
    val ussdGetSimCharge: String,
    val taxPercent: Int,
    val minChargeAmount: Long?,
)

data class ChargeProduct(
    val id: String,
    val kind: ChargeKind,
    val providerId: String,
    val amountRials: Long?,
    val categoryId: String,
    val serviceTypeCode: String,
    val loadUssd: String,
    val labelFa: String,
    val groupLabelFa: String,
    val hasCount: Boolean,
    val labelEn: String = "",
    val groupLabelEn: String = "",
)

/** نام اپراتور به زبان اپ: انگلیسی (`en`) یا فارسی (`fn`) از ChargeList. */
fun ChargeOperator.displayName(english: Boolean): String =
    if (english) nameEn.ifBlank { nameFa } else nameFa.ifBlank { nameEn }

/** برچسب محصول (مثلاً «شارژ معمولی» / «Normal Charge») به زبان اپ. */
fun ChargeProduct.displayLabel(english: Boolean): String =
    if (english) labelEn.ifBlank { labelFa } else labelFa.ifBlank { labelEn }

/** برچسب گروه محصول (مثلاً «شارژ شگفت انگیز» / «Incredible Charge») به زبان اپ. */
fun ChargeProduct.displayGroupLabel(english: Boolean): String =
    if (english) groupLabelEn.ifBlank { groupLabelFa } else groupLabelFa.ifBlank { groupLabelEn }

/**
 * فهرست شارژ خرید کد و تاپ‌آپ. سداد از ChargeList پر می‌شود؛ PSPهای دیگر خالی‌اند.
 */
interface ChargeCatalog {
    fun hasEntries(): Boolean = products().isNotEmpty()

    fun products(): List<ChargeProduct>

    fun findProduct(productId: String): ChargeProduct? =
        products().firstOrNull { it.id == productId }

    fun operators(kind: ChargeKind): List<ChargeOperator>

    fun products(kind: ChargeKind, providerId: String): List<ChargeProduct> =
        products().filter { it.kind == kind && it.providerId == providerId }

    fun productGroups(kind: ChargeKind, providerId: String): List<String> =
        products(kind, providerId).map { it.groupLabelFa }.distinct()
}
