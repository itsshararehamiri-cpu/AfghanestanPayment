package com.danesh.hp.voucher

import com.danesh.api.CouponCatalog
import com.danesh.api.CouponProduct
import javax.inject.Inject
import javax.inject.Singleton

/** این PSP فهرست کالای کالابرگ محلی ندارد. */
@Singleton
class EmptyCouponCatalog @Inject constructor() : CouponCatalog {
    override fun products(): List<CouponProduct> = emptyList()
}
