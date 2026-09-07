package com.danesh.hp.config

import java.security.MessageDigest
import java.util.Locale

/**
 * هش فعال‌سازی (Activation hash) پیکربندی ترمینال برای سوییچ کارن (KAREN) — تگ 007 فیلد 72.
 *
 * پیاده‌سازی مطابق فرمول terminal_config_compute_hash مستند پروتکل KAREN:
 * SHA-256("DE26=<mcc>\nDE41=<terminalId>\nDE42=<merchantId>\nDE43=<merchantNameLocation>\n" + canonicalTlv)
 * که خروجی آن هگزادسیمال ۶۴ نویسه با حروف بزرگ است.
 */
object HpTerminalConfigHash {

    fun compute(
        mcc: String,
        terminalId: String,
        merchantId: String,
        merchantNameLocation: String,
        canonicalTlv: String,
    ): String {
        val prefix = "DE26=$mcc\nDE41=$terminalId\nDE42=$merchantId\nDE43=$merchantNameLocation\n"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((prefix + canonicalTlv).toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte ->
            String.format(Locale.US, "%02X", byte)
        }
    }
}
