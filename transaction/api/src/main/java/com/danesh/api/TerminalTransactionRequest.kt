package com.danesh.api

data class LogonRequest(
    val id: String = "",
    /** اگر مقدار داشته باشد، کف/سقف رسید اختیاری همراه LOGON برای سوئیچ ارسال می‌شود. */
    val optionalReceipt: OptionalReceiptLimits? = null,
) : TransactionRequest

data class InitRequest(
    /** بلیط اول — PoR = SHA256(Ticket_1 + Serial)؛ در F61 فقط PoR ارسال می‌شود */
    val firstBallotTicket: String,
    /** بلیط دوم — PoA = SHA256(Ticket_2 + Serial) برای اعتبارسنجی پاسخ Init */
    val secondBallotTicket: String,
) : TransactionRequest
