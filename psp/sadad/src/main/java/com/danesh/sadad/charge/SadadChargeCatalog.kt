package com.danesh.sadad.charge

import android.content.Context
import com.danesh.api.ChargeCatalog
import com.danesh.api.ChargeKind
import com.danesh.api.ChargeOperator
import com.danesh.api.ChargeProduct
import com.danesh.sadad.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadChargeCatalog @Inject constructor(
    @ApplicationContext context: Context,
) : ChargeCatalog {

    private val snapshot: SadadChargeListSnapshot by lazy {
        context.resources.openRawResource(R.raw.sadad_charge_list).use { SadadChargeListParser.parse(it) }
    }

    override fun products(): List<ChargeProduct> = snapshot.products

    override fun operators(kind: ChargeKind): List<ChargeOperator> = when (kind) {
        ChargeKind.VOUCHER -> snapshot.voucherOperators
        ChargeKind.TOPUP -> snapshot.topUpOperators
    }
}
