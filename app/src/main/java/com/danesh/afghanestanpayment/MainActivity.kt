package com.danesh.afghanestanpayment

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.afghanestanpayment.config.AppRuntimeConfig
import com.danesh.afghanestanpayment.config.toReceiptPspBrand
import com.danesh.afghanestanpayment.navigation.AppNavHost
import com.danesh.afghanestanpayment.ui.theme.AfghanestanPaymentTheme
import com.danesh.api.CurrencyDefaultsProvider
import com.danesh.api.ReceiptFeeDefaultsProvider
import com.danesh.balance.navigation.getCurrentDate
import com.danesh.common.currency.LocalCurrencyLabel
import com.danesh.common.locale.LocalReceiptCalendarStyle
import com.danesh.common.locale.LocalReceiptLocale
import com.danesh.common.locale.LocaleManager
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.toAppLocale
import com.danesh.common.locale.toComposeLayoutDirection
import com.danesh.common.locale.toReceiptCalendarStyle
import com.danesh.common.merchant.LocalMicroPaymentIndexConfig
import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.common.merchant.rememberMicroPaymentIndexConfig
import com.danesh.common.network.openNetworkSettings
import com.danesh.common.receipt.LocalBalanceTransactionFee
import com.danesh.common.receipt.LocalMerchantReceiptPrintMode
import com.danesh.common.receipt.LocalReceiptPspBrand
import com.danesh.common.receipt.MerchantReceiptPrintPreferences
import com.danesh.common.receipt.rememberCurrentMerchantReceiptPrintMode
import com.danesh.common.ui.NetworkErrorScreen
import com.danesh.core.Device
import com.danesh.splash.SplashScreen
import com.danesh.ui.theme.LocalAppUiLayoutDirection
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import org.jpos.iso.ISOUtil
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var appRuntimeConfig: AppRuntimeConfig
    @Inject lateinit var localePreferences: LocalePreferences
    @Inject lateinit var currencyDefaults: CurrencyDefaultsProvider
    @Inject lateinit var receiptFeeDefaults: ReceiptFeeDefaultsProvider
    @Inject lateinit var merchantReceiptPrintPreferences: MerchantReceiptPrintPreferences
    @Inject lateinit var merchantDisplayPreferences: MerchantDisplayPreferences
    @Inject lateinit var device: Device

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val cipher = Cipher.getInstance("DESede/ECB/NoPadding")

        val key = SecretKeySpec(
            ISOUtil.hex2byte("0123456789ABCDEFFEDCBA9876543210"),
            "DESede"
        )

        cipher.init(Cipher.DECRYPT_MODE, key)

        val plainMak = cipher.doFinal(ISOUtil.hex2byte("31A7364CAC91CA39C0489F69BEC54FA2"))
        Log.d("TAG", "onCreate: hhhhh${ISOUtil.hexString(plainMak)}")
        if (intent.getBooleanExtra(BootLaunchService.EXTRA_LAUNCHED_FROM_BOOT, false)) {
            BootForegroundHelper.apply(this)
        }
        device.registerBootAutoStart()
        LocaleManager.apply(localePreferences.getLanguage())

        enableEdgeToEdge()
        hideNavigationBar()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        getCurrentDate()
        setContent {
            HideSystemBars()

            var splashCompleted by rememberSaveable { mutableStateOf(false) }
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
                LocalCurrencyLabel provides currencyDefaults.currencyLabel,
                LocalBalanceTransactionFee provides receiptFeeDefaults.balanceTransactionFee,
                LocalMerchantReceiptPrintMode provides merchantReceiptPrintMode,
                LocalMicroPaymentIndexConfig provides microPaymentIndexConfig,
            ) {
                AfghanestanPaymentTheme {
                    if (!splashCompleted) {
                        SplashScreen()
                        LaunchedEffect(Unit) {
                             delay(SPLASH_DURATION_MS)
                            splashCompleted = true
                        }
                    } else {
                        LaunchContent(onExitClick = { finish() })
                    }
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideNavigationBar()
        if (intent.getBooleanExtra(BootLaunchService.EXTRA_LAUNCHED_FROM_BOOT, false)) {
            BootForegroundHelper.apply(this)
            intent.removeExtra(BootLaunchService.EXTRA_LAUNCHED_FROM_BOOT)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideNavigationBar()
        }
    }

    private fun hideNavigationBar() {
        lockNavigationBar(device)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun LaunchContent(onExitClick: () -> Unit) {
        val launchViewModel: LaunchConnectivityViewModel = hiltViewModel()
        val isConnected by launchViewModel.isConnected.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    launchViewModel.refresh()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        if (!isConnected) {
            val wifiEnabled = launchViewModel.isWifiEnabled()
            NetworkErrorScreen(
                title = stringResource(com.danesh.common.R.string.network_error_title),
                message = stringResource(
                    if (wifiEnabled) {
                        com.danesh.common.R.string.network_error_no_internet
                    } else {
                        com.danesh.common.R.string.network_error_wifi_disabled
                    },
                ),
                primaryButtonText = stringResource(
                    if (wifiEnabled) {
                        com.danesh.common.R.string.action_retry
                    } else {
                        com.danesh.common.R.string.network_error_enable_wifi
                    },
                ),
                onPrimaryClick = {
                    if (wifiEnabled) {
                        launchViewModel.refresh()
                    } else {
                        openNetworkSettings(context)
                    }
                },
                onCancelClick = onExitClick,
                onBackClick = null,
            )
        } else {
            AppNavHost(onExitClick = onExitClick)
        }
    }

    @Composable
    private fun HideSystemBars() {
        DisposableEffect(Unit) {
            hideNavigationBar()
            onDispose { }
        }
    }

    private companion object {
        const val SPLASH_DURATION_MS = 3_000L
    }
}
