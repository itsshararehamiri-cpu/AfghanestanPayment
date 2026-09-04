package com.danesh.bp.bootstrap

import com.danesh.bp.mac.BpMacCalculator
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpBootstrapResponseValidator @Inject constructor(
    private val macCalculator: BpMacCalculator,
) {

    suspend fun validateInitResponse(
        requestStan: String,
        response: IsoMessage,
    ) {
        validateCommon(
            response = response,
            expectedMti = "0810",
            expectedProcessingCode = "900000",
            requestStan = requestStan,
        )
        macCalculator.verifyResponseMac(response)
    }

    suspend fun validateLogonResponse(
        requestStan: String,
        response: IsoMessage,
    ) {
        validateCommon(
            response = response,
            expectedMti = "0810",
            expectedProcessingCode = "920000",
            requestStan = requestStan,
        )
        macCalculator.verifyResponseMac(response)
    }

    private fun validateCommon(
        response: IsoMessage,
        expectedMti: String,
        expectedProcessingCode: String,
        requestStan: String,
    ) {
        check(response.mti == expectedMti) {
            "MTI پاسخ نامعتبر: ${response.mti} (انتظار $expectedMti)"
        }
        val processingCode = response.processingCode
        if (processingCode.isNotBlank()) {
            check(processingCode == expectedProcessingCode) {
                "کد پردازش پاسخ نامعتبر: $processingCode (انتظار $expectedProcessingCode)"
            }
        }
        val responseStan = response.stan
        if (responseStan.isNotBlank() && requestStan.isNotBlank()) {
            check(responseStan == requestStan) {
                "STAN پاسخ با درخواست مطابقت ندارد: $responseStan != $requestStan"
            }
        }
        check(response.responseCode == "00") {
            "کد پاسخ ناموفق: ${response.responseCode}"
        }
    }
}
