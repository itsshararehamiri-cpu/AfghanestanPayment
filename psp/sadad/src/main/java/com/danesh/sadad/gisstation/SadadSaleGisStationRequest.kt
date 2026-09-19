package com.danesh.sadad.gisstation

import com.danesh.api.TransactionRequest

/** 24-SALE GIS STATION (MTI 0200/0210، DE3 740000). */
data class SadadSaleGisStationRequest(
    val track2: String,
    val pinBlock: String,
    val amount: String,
) : TransactionRequest
