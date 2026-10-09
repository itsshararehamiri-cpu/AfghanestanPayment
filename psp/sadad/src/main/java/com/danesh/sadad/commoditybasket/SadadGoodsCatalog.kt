package com.danesh.sadad.commoditybasket

import android.content.Context
import android.util.Log
import com.danesh.api.CouponCatalog
import com.danesh.api.CouponProduct
import com.danesh.sadad.R
import com.danesh.sadad.charge.XmlNode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * فهرست کالاهای کالابرگ سداد: هر `MENUITEM` که `SERVICEDESCRIPTOR` آن `SERVICE="GOODBASKET"` دارد.
 *
 * ```
 * <MENUITEM CLASS="" FN="ماست" ENABLE="TRUE" HIDDEN="FALSE" EN="YOGURT" SERVICEIDENTIFIER="6612569878415">
 *   <SERVICEDESCRIPTOR SERVICE="GOODBASKET" />
 * </MENUITEM>
 * ```
 *
 * attribute `ENABLE` فهرست روی نمایش کالا اثری ندارد؛ فعال/غیرفعال بودن را پذیرنده
 * در تنظیمات کالابرگ تعیین می‌کند. «سایر» (کد [CouponCatalog.OTHER_BARCODE]) همیشه آخر فهرست است.
 */
@Singleton
class SadadGoodsCatalog @Inject constructor(
    @ApplicationContext private val context: Context,
) : CouponCatalog {

    private val products: List<CouponProduct> by lazy {
        val parsed = runCatching {
            context.resources.openRawResource(R.raw.sadad_goods_list).use { SadadGoodsListParser.parse(it.readBytes()) }
        }.onFailure { Log.e(TAG, "GOODBASKET list load failed", it) }
            .getOrDefault(emptyList())
        SadadGoodsListParser.withOther(parsed)
    }

    override fun products(): List<CouponProduct> = products

    private companion object {
        const val TAG = "SadadGoodsCatalog"
    }
}

internal object SadadGoodsListParser {
    private const val SERVICE_GOODBASKET = "GOODBASKET"

    /** کد واحد «تعدادی» (مبلغ × تعداد). */
    const val UNIT_COUNT = "01"

    /** کد واحد کالاهای غیرتعدادی. */
    const val UNIT_AMOUNT = "02"

    private val countableNames = listOf("پوشک", "شیر خشک", "شیرخشک")

    fun parse(bytes: ByteArray): List<CouponProduct> {
        var content = bytes
        if (content.size >= 3 && content[0] == 0xEF.toByte() && content[1] == 0xBB.toByte() && content[2] == 0xBF.toByte()) {
            content = content.copyOfRange(3, content.size)
        }
        val root = XmlNode.parse(content.toString(Charsets.UTF_8))
        return root.allDescendants()
            .filter { it.tag.equals("MENUITEM", ignoreCase = true) && it.isGoodBasket() }
            .mapNotNull { it.toProduct() }
            .filter { it.barcode != CouponCatalog.OTHER_BARCODE }
            .distinctBy { it.barcode }
    }

    fun withOther(products: List<CouponProduct>): List<CouponProduct> =
        products + CouponProduct(
            barcode = CouponCatalog.OTHER_BARCODE,
            nameFa = "سایر",
            nameEn = "Other",
            countable = true,
            unitCode = UNIT_COUNT,
            isOther = true,
        )

    private fun XmlNode.isGoodBasket(): Boolean = children.any { child ->
        child.tag.equals("SERVICEDESCRIPTOR", ignoreCase = true) &&
            child.attrIgnoreCase("SERVICE").equals(SERVICE_GOODBASKET, ignoreCase = true)
    }

    private fun XmlNode.toProduct(): CouponProduct? {
        val barcode = attrIgnoreCase("SERVICEIDENTIFIER").filter(Char::isDigit)
        if (barcode.isEmpty()) return null
        val nameFa = attrIgnoreCase("FN").trim()
        val nameEn = attrIgnoreCase("EN").trim()
        val explicitCountable = attrIgnoreCase("COUNTABLE").takeIf { it.isNotBlank() }
            ?.equals("TRUE", ignoreCase = true)
        val countable = explicitCountable ?: countableNames.any { nameFa.contains(it) }
        val unit = attrIgnoreCase("UNIT").filter(Char::isDigit).takeIf { it.isNotEmpty() }
            ?.padStart(2, '0')?.takeLast(2)
            ?: if (countable) UNIT_COUNT else UNIT_AMOUNT
        return CouponProduct(
            barcode = barcode,
            nameFa = nameFa,
            nameEn = nameEn,
            countable = countable,
            unitCode = unit,
        )
    }
}
