package com.danesh.bp.field48

import com.danesh.api.TerminalConfig
import com.danesh.api.TransactionClock
import com.danesh.api.TransactionContextProvider
import com.danesh.iso.BpIsoMessage
import com.danesh.iso.field48.BpField48Tlv
import kotlin.text.Charsets
import org.junit.Assert.assertEquals
import org.junit.Test

class BpMerchantInfoPersisterTest {

    @Test
    fun persistFromResponse_savesMerchantTagsFromField48() {
        val context = RecordingContextProvider()
        val persister = BpMerchantInfoPersister(context)
        val response = BpIsoMessage(BpField48Tlv()).apply {
            setField48 {
                setField48Tag(BpField48Tags.MERCHANT_NAME_FA, "فروشگاه تست")
                setField48Tag(BpField48Tags.MERCHANT_PHONE, "02112345678")
                setField48Tag(BpField48Tags.MERCHANT_ADDRESS, "تهران")
                setField48Tag(BpField48Tags.MERCHANT_NAME_EN, "Test Shop")
                setField48Tag(BpField48Tags.MERCHANT_POSTAL_CODE, "1234567890")
            }
        }

        persister.persistFromResponse(response)

        val saved = context.saved ?: error("config not saved")
        assertEquals("فروشگاه تست", saved.merchantName)
        assertEquals("02112345678", saved.merchantPhone)
        assertEquals("تهران", saved.merchantAddress)
        assertEquals("Test Shop", saved.englishMerchantName)
        assertEquals("1234567890", saved.merchantPostalCode)
    }

    @Test
    fun persistFromResponse_decodesCp1256MojibakeMerchantNames() {
        val context = RecordingContextProvider()
        val persister = BpMerchantInfoPersister(context)
        val cp1256 = java.nio.charset.Charset.forName("cp1256")
        val faName = "فروشگاه تست"
        val faMojibake = String(faName.toByteArray(cp1256), Charsets.ISO_8859_1)
        val response = BpIsoMessage(BpField48Tlv()).apply {
            setField48 {
                setField48Tag(BpField48Tags.MERCHANT_NAME_FA, faMojibake)
                setField48Tag(BpField48Tags.MERCHANT_NAME_EN, "Test Shop")
            }
        }

        persister.persistFromResponse(response)

        val saved = context.saved ?: error("config not saved")
        assertEquals(faName, saved.merchantName)
        assertEquals("Test Shop", saved.englishMerchantName)
    }

    private class RecordingContextProvider : TransactionContextProvider {
        private var config = TerminalConfig(
            terminalId = "T1",
            merchantId = "M1",
            merchantName = "",
            merchantPhone = "",
            nii = "0009",
            pointOfServiceEntryMode = "021",
            currency = "364",
        )
        var saved: TerminalConfig? = null

        override fun getTerminalConfig(): TerminalConfig = config

        override fun saveTerminalConfig(config: TerminalConfig) {
            saved = config
            this.config = config
        }

        override fun nextStan(): String = "000001"

        override fun currentClock(): TransactionClock = TransactionClock("20260711", "120000")

        override fun saveVatPercentage(varPercentage: String) = Unit

        override fun getVatPercentage(): String = ""

        override fun lastSuccessfulStan(): String = "000000"

        override fun lastSuccessfulRrn(): String = "000000000000"

        override fun saveLastSuccessfulTransaction(stan: String, rrn: String?) = Unit

        override fun clearTerminalData() = Unit
    }
}
