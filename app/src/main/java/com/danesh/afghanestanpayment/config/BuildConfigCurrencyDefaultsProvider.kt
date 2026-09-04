package com.danesh.afghanestanpayment.config

import com.danesh.afghanestanpayment.BuildConfig
import com.danesh.api.CurrencyDefaultsProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildConfigCurrencyDefaultsProvider @Inject constructor() : CurrencyDefaultsProvider {
    override val currencyCode: String = BuildConfig.DEFAULT_CURRENCY
    override val currencyLabel: String = BuildConfig.CURRENCY_LABEL
}
