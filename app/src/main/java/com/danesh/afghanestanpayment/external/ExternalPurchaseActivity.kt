package com.danesh.afghanestanpayment.external

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import com.danesh.afghanestanpayment.lockNavigationBar
import com.danesh.afghanestanpayment.config.AppRuntimeConfig
import com.danesh.afghanestanpayment.config.toReceiptPspBrand
import com.danesh.afghanestanpayment.ui.theme.AfghanestanPaymentTheme
import com.danesh.ui.theme.LocalAppUiLayoutDirection
import com.danesh.api.convertPurchaseResultToJsonObject
import com.danesh.common.locale.LocalReceiptCalendarStyle
import com.danesh.common.locale.LocalReceiptLocale
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.toAppLocale
import com.danesh.common.locale.toComposeLayoutDirection
import com.danesh.common.locale.toReceiptCalendarStyle
import com.danesh.common.receipt.GlobalPrintErrorHost
import com.danesh.common.merchant.LocalMicroPaymentIndexConfig
import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.common.merchant.rememberMicroPaymentIndexConfig
import com.danesh.common.receipt.LocalMerchantReceiptPrintMode
import com.danesh.common.receipt.LocalReceiptPspBrand
import com.danesh.common.receipt.MerchantReceiptPrintPreferences
import com.danesh.common.receipt.rememberCurrentMerchantReceiptPrintMode
import com.danesh.core.Device
import com.danesh.purchase.navigation.PurchaseNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExternalPurchaseActivity : AppCompatActivity() {

    @Inject lateinit var appRuntimeConfig: AppRuntimeConfig
    @Inject lateinit var localePreferences: LocalePreferences
    @Inject lateinit var merchantReceiptPrintPreferences: MerchantReceiptPrintPreferences
    @Inject lateinit var merchantDisplayPreferences: MerchantDisplayPreferences
    @Inject lateinit var device: Device

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lockNavigationBar(device)
        val amount = intent.readAmount()
        if (amount.isNullOrBlank()) {
            finishCancelled()
            return
        }

        setContent {
            val layoutDirection = localePreferences.getLanguage().toComposeLayoutDirection()
            val merchantReceiptPrintMode = rememberCurrentMerchantReceiptPrintMode(
                merchantReceiptPrintPreferences,
            )
            val microPaymentIndexConfig = rememberMicroPaymentIndexConfig(merchantDisplayPreferences)
            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection,
                LocalAppUiLayoutDirection provides layoutDirection,
                LocalReceiptLocale provides localePreferences.getLanguage().toAppLocale(),
                LocalReceiptCalendarStyle provides localePreferences.getLanguage().toReceiptCalendarStyle(),
                LocalReceiptPspBrand provides appRuntimeConfig.activePsp.toReceiptPspBrand(),
                LocalMerchantReceiptPrintMode provides merchantReceiptPrintMode,
                LocalMicroPaymentIndexConfig provides microPaymentIndexConfig,
            ) {
                AfghanestanPaymentTheme {
                    GlobalPrintErrorHost()
                    PurchaseNavHost(
                        onFlowComplete = ::finishCancelled,
                        externalPurchaseAmount = amount,
                        onExternalPurchaseComplete = ::finishWithPurchaseResult,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lockNavigationBar(device)
    }

    private fun finishWithPurchaseResult(responseJson: String) {
        val callerPayload = convertPurchaseResultToJsonObject(responseJson)
        setResult(
            RESULT_OK,
            Intent().putExtra(ExternalPurchaseContract.EXTRA_RESPONSE, callerPayload),
        )
        finish()
    }

    private fun finishCancelled() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun Intent.readAmount(): String? {
        getStringExtra(ExternalPurchaseContract.EXTRA_AMOUNT)?.trim()?.takeIf { it.isNotEmpty() }
            ?.let { return it }
        val numericAmount = getLongExtra(ExternalPurchaseContract.EXTRA_AMOUNT, -1L)
        return if (numericAmount >= 0L) numericAmount.toString() else null
    }
}
