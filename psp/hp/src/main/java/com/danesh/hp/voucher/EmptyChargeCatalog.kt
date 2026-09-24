package com.danesh.hp.voucher

import com.danesh.api.ChargeCatalog
import com.danesh.api.ChargeKind
import com.danesh.api.ChargeOperator
import com.danesh.api.ChargeProduct
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmptyChargeCatalog @Inject constructor() : ChargeCatalog {
    override fun products(): List<ChargeProduct> = emptyList()
    override fun operators(kind: ChargeKind): List<ChargeOperator> = emptyList()
}
