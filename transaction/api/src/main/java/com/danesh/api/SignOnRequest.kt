package com.danesh.api


data class SignOnRequest(
    /** بلیط اول — PoR = SHA256(Ticket_1 + Serial)؛ در F61 فقط PoR ارسال می‌شود */
    val firstBallotTicket: String,
    /** بلیط دوم — PoA = SHA256(Ticket_2 + Serial) برای اعتبارسنجی پاسخ Init */
    val secondBallotTicket: String,
) : TransactionRequest
