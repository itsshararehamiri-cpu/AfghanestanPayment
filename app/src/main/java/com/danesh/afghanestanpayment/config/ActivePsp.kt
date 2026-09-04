package com.danesh.afghanestanpayment.config

enum class ActivePsp {
    HP,
    FANAVA,
    AP,
    PN,
    BP;


    val isHamrahPay: Boolean
        get() = this == HP || this == FANAVA || this == AP || this == PN

    val isBehpardakht: Boolean
        get() = this == BP
}
