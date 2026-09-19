package com.danesh.sadad.fuelstation

import com.danesh.api.TransactionRequest

/** 26-FUEL STATION INQUIRY (MTI 0100/0110، DE3 240000). */
data class SadadFuelStationInquiryRequest(
    val track2: String,
    val amount: String,
) : TransactionRequest
