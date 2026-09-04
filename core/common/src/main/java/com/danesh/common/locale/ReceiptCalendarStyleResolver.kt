package com.danesh.common.locale

fun AppLanguage.toReceiptCalendarStyle(): ReceiptCalendarStyle = when (this) {
    AppLanguage.English -> ReceiptCalendarStyle.GREGORIAN
    AppLanguage.PersianPashto -> ReceiptCalendarStyle.AFGHAN_SOLAR
    AppLanguage.PersianDari,
    AppLanguage.Persian,
    AppLanguage.Other,
    -> ReceiptCalendarStyle.AFGHAN_SOLAR
}

fun AppLocale.toReceiptCalendarStyle(): ReceiptCalendarStyle = when (this) {
    AppLocale.ENGLISH -> ReceiptCalendarStyle.GREGORIAN
    AppLocale.PASHTO -> ReceiptCalendarStyle.AFGHAN_SOLAR
    AppLocale.DARI,
    AppLocale.IRANIAN,
    -> ReceiptCalendarStyle.AFGHAN_SOLAR
}
