package com.danesh.sadad.commoditybasket

import com.danesh.api.CouponCatalog
import com.danesh.api.CouponOrderItem
import com.danesh.sadad.util.FunctionCodeData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadCommodityBasketCodecTest {

    @Test
    fun inquiryDataMatchesDocumentSample() {
        val data = SadadCommodityBasketCodec.inquiryData(
            listOf(
                CouponOrderItem("6619988178317", quantity = 1, amount = "0", unitCode = "01"),
                CouponOrderItem("6655463344853", quantity = 2, amount = "0", unitCode = "01"),
            ),
        )
        // نمونهٔ سند: 01 046 045 + این داده
        assertEquals("002661998817831701000100665546334485301000200", data)
    }

    @Test
    fun quoteRoundTrip() {
        val data = SadadCommodityBasketCodec.saleData(4_444_444, 4_000_000, "1240483364594")
        val quote = SadadCommodityBasketCodec.parseQuote(listOf(FunctionCodeData("047", data)))!!
        assertEquals(4_444_444, quote.transactionAmount)
        assertEquals(4_000_000, quote.creditAmount)
        assertEquals("1240483364594", quote.traceItem)
        assertEquals(444_444, quote.cashAmount)
    }

    @Test
    fun missingQuoteIsNull() {
        assertNull(SadadCommodityBasketCodec.parseQuote(listOf(FunctionCodeData("029", "xx"))))
    }

    @Test
    fun goodsListParsesGoodBasketItemsOnly() {
        val xml = """
            <MENU>
              <MENUITEM CLASS="" FN="ماست" ENABLE="FALSE" HIDDEN="FALSE" EN="YOGURT" SERVICEIDENTIFIER="6612569878415">
                <SERVICEDESCRIPTOR SERVICE="GOODBASKET" />
              </MENUITEM>
              <MENUITEM FN="پوشک بچه" EN="DIAPER" SERVICEIDENTIFIER="6600000000001">
                <SERVICEDESCRIPTOR SERVICE="GOODBASKET" />
              </MENUITEM>
              <MENUITEM FN="شارژ" SERVICEIDENTIFIER="123">
                <SERVICEDESCRIPTOR SERVICE="NormalCharge" />
              </MENUITEM>
            </MENU>
        """.trimIndent()
        val products = SadadGoodsListParser.withOther(SadadGoodsListParser.parse(xml.toByteArray()))
        assertEquals(listOf("6612569878415", "6600000000001", CouponCatalog.OTHER_BARCODE), products.map { it.barcode })
        assertFalse(products[0].countable)
        assertTrue(products[1].countable)
        assertTrue(products.last().isOther && products.last().countable)
    }
}
