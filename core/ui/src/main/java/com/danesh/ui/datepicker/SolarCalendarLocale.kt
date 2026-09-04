package com.danesh.ui.datepicker

import saman.zamani.persiandate.PersianDate

enum class SolarCalendarLocale {
    IRANIAN,
    DARI,
    PASHTO;


    fun monthName(month: Int): String {
        val index = month - 1
        return when (this) {
            IRANIAN, DARI -> PersianDate().apply {
                shYear = 1400
                shMonth = month
                shDay = 1
            }.monthName
            PASHTO -> PASHTO_MONTHS.getOrElse(index) { "" }
        }
    }

    private companion object {
        private val PASHTO_MONTHS = arrayOf(
            "وری", "غویی", "غبرګولی", "چنګاښ", "زمرګ", "وږی",
            "تله", "لړم", "لیندۍ", "مرغومی", "سلواغه", "کب",
        )
    }
}
