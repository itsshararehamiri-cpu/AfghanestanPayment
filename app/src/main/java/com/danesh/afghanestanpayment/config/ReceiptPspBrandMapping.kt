package com.danesh.afghanestanpayment.config

import com.danesh.common.receipt.ReceiptPspBrand

fun ActivePsp.toReceiptPspBrand(): ReceiptPspBrand = when (this) {
    ActivePsp.HP -> ReceiptPspBrand.HP
    ActivePsp.FANAVA -> ReceiptPspBrand.FANAVA
    ActivePsp.AP -> ReceiptPspBrand.AP
    ActivePsp.PN -> ReceiptPspBrand.PN
    ActivePsp.BP -> ReceiptPspBrand.BP
    ActivePsp.SADAD -> ReceiptPspBrand.SADAD
}
