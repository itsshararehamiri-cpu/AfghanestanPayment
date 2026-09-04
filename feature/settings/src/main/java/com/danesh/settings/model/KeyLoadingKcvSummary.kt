package com.danesh.settings.model

data class KeyLoadingKcvSummary(
    val master: String,
    val mac: String = "-",
    val pin: String = "-",
    val data: String = "-",
    /** پس از Init به‌پرداخت فقط TMK پایانه روی PED است — MAC/PIK/DEK بعد از Logon می‌آیند. */
    val masterOnly: Boolean = false,
)
