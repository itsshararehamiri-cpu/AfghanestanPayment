package com.danesh.afghanestanpayment.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danesh.afghanestanpayment.BuildConfig
import com.danesh.balance.navigation.BalanceNavHost
import com.danesh.card_to_card.navigation.CardToCardNavHost
import com.danesh.cashdeposit.navigation.CashDepositNavHost
import com.danesh.cashout.navigation.CashOutNavHost
import com.danesh.common.locale.displayMerchantName
import com.danesh.common.network.openNetworkSettings
import com.danesh.common.receipt.GlobalPrintErrorHost
import com.danesh.common.receipt.PendingSafReceiptGate
import com.danesh.menu.HomeMenuScreen
import com.danesh.menu.HomeMenuViewModel
import com.danesh.menu.model.MenuItemType
import com.danesh.purchase.navigation.PurchaseNavHost
import com.danesh.report.ReportNavHost
import com.danesh.settings.RoleSelectionBottomSheet
import com.danesh.settings.model.AppRole
import com.danesh.settings.navigation.SettingsNavArgs
import com.danesh.settings.navigation.SettingsNavHost
import com.danesh.support.navigation.SupportDirectLaunch
import com.danesh.support.navigation.SupportNavHost
import com.danesh.topup.navigation.TopUpNavHost
import com.danesh.voucher.navigation.VoucherNavHost
import com.danesh.wallet_to_wallet.navigation.WalletToWalletNavHost
import com.example.bill.navigation.BillNavHost

object AppRoutes {
    const val HOME = "home"
    const val BALANCE = "balance"
    const val PURCHASE = "purchase"
    const val BILL = "bill"
    const val CARD_TO_CARD = "card_to_card"
    const val WALLET_TO_WALLET = "wallet_to_wallet"
    const val TOP_UP = "top_up"
    const val VOUCHER = "voucher"

    const val SUPPORT = "support"
    const val SUPPORT_SERVICE =
        "support_service/{amount}/{serviceId}/{title}/{track2}/{pan}"
    const val CASH_DEPOSIT = "cash_deposit"
    const val CASH_OUT = "cash_out"
    const val SETTINGS = "settings/{${SettingsNavArgs.ROLE}}"
    const val REPORT = "report"

    fun settings(role: AppRole): String = "settings/${role.name}"

    fun supportService(
        amount: String,
        serviceId: String,
        title: String,
        track2: String = "",
        pan: String = "",
    ): String =
        "support_service/${Uri.encode(amount)}/${Uri.encode(serviceId)}/${Uri.encode(title)}/${Uri.encode(track2)}/$pan"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(onExitClick:()-> Unit) {
    GlobalPrintErrorHost()
    PendingSafReceiptGate {
        AppNavHostContent(onExitClick={onExitClick()})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AppNavHostContent(onExitClick:()-> Unit) {
    val navController = rememberNavController()
    var showRoleSelectionSheet by rememberSaveable { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
    ) {
        composable(AppRoutes.HOME) {
            val homeViewModel: HomeMenuViewModel = hiltViewModel()
            val terminalConfig by homeViewModel.terminalConfig.collectAsStateWithLifecycle()
            val isConfigured by homeViewModel.isConfigured.collectAsStateWithLifecycle()
            val visibleMenuItems by homeViewModel.visibleMenuItems.collectAsStateWithLifecycle()
            val isConnected by homeViewModel.isConnected.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        homeViewModel.refresh()
                        homeViewModel.refreshNetwork()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            HomeMenuScreen(
                terminalId = terminalConfig.terminalId,
                merchantName = displayMerchantName(
                    terminalConfig.merchantName,
                    terminalConfig.englishMerchantName,
                ),
                qrContent = "terminal:${terminalConfig.terminalId}",
                menuItems = visibleMenuItems,
                isConfigured = isConfigured,
                onConfigurationClick = {
                    navController.navigate(AppRoutes.settings(AppRole.Support))
                },
                isConnected = isConnected,
                isWifiEnabled = { homeViewModel.isWifiEnabled() },
                onRefreshNetwork = { homeViewModel.refreshNetwork() },
                onOpenWifiSettings = { openNetworkSettings(context) },
                onMenuItemClick = { item ->
                    when (item) {
                        MenuItemType.BALANCE -> navController.navigate(AppRoutes.BALANCE)
                        MenuItemType.PURCHASE -> navController.navigate(AppRoutes.PURCHASE)
                        MenuItemType.BILL -> navController.navigate(AppRoutes.BILL)
                        MenuItemType.TRANSFER -> navController.navigate(AppRoutes.CARD_TO_CARD)
                        MenuItemType.WALLET_TO_WALLET -> navController.navigate(AppRoutes.WALLET_TO_WALLET)
                        MenuItemType.TOPUP -> navController.navigate(AppRoutes.TOP_UP)
                        MenuItemType.VOUCHER -> navController.navigate(AppRoutes.VOUCHER)

                        MenuItemType.SUPPORT -> navController.navigate(AppRoutes.SUPPORT)
                        MenuItemType.CASH_DEPOSIT -> navController.navigate(AppRoutes.CASH_DEPOSIT)
                        MenuItemType.CASH_OUT -> navController.navigate(AppRoutes.CASH_OUT)
                        MenuItemType.SETTINGS -> showRoleSelectionSheet = true
                        MenuItemType.REPORT -> navController.navigate(AppRoutes.REPORT)
                        MenuItemType.CHANGE_ACCOUNT -> {}
                    }
                },
                onQrClick = {},
            )

            if (showRoleSelectionSheet) {
                RoleSelectionBottomSheet(
                    appVersion = BuildConfig.VERSION_NAME,
                    onRoleSelected = { role ->
                        showRoleSelectionSheet = false
                        navController.navigate(AppRoutes.settings(role))
                    },
                    onDismissRequest = { showRoleSelectionSheet = false },
                )
            }
        }
        composable(AppRoutes.BALANCE) {
            BalanceNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
            )
        }
        composable(AppRoutes.PURCHASE) {
            PurchaseNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
            )
        }
        composable(AppRoutes.BILL) {
            BillNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
            )
        }
        composable(AppRoutes.CARD_TO_CARD) {
            CardToCardNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
            )
        }
        composable(AppRoutes.WALLET_TO_WALLET) {
            WalletToWalletNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
            )
        }
        composable(AppRoutes.TOP_UP) {
            TopUpNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
            )
        }
        composable(AppRoutes.VOUCHER) {
            VoucherNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
            )
        }
        composable(AppRoutes.SUPPORT) {
            SupportNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
            )
        }
        composable(
            route = AppRoutes.SUPPORT_SERVICE,
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("track2") { type = NavType.StringType },
                navArgument("pan") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount").orEmpty()
            val serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty()
            val title = backStackEntry.arguments?.getString("title").orEmpty()
            val track2 = backStackEntry.arguments?.getString("track2").orEmpty()
            val pan = backStackEntry.arguments?.getString("pan").orEmpty()
            SupportNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
                directLaunch = SupportDirectLaunch(
                    amount = amount,
                    serviceId = serviceId,
                    title = title,
                    track2 = track2,
                    pan = pan,
                ),
            )
        }
        composable(AppRoutes.CASH_DEPOSIT) {
            CashDepositNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
            )
        }
        composable(AppRoutes.CASH_OUT) {
            CashOutNavHost(
                onFlowComplete = { navController.popBackStackIfAvailable() },
            )
        }
        composable(
            route = AppRoutes.SETTINGS,
            arguments = listOf(
                navArgument(SettingsNavArgs.ROLE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val roleName = backStackEntry.arguments?.getString(SettingsNavArgs.ROLE).orEmpty()
            val role = runCatching { AppRole.valueOf(roleName) }.getOrDefault(AppRole.Merchant)
            SettingsNavHost(
                role = role,
                onFlowComplete = { navController.popBackStackIfAvailable() },
                onLaunchSupportService = { request ->
                    navController.navigate(
                        AppRoutes.supportService(
                            amount = request.amount,
                            serviceId = request.serviceId,
                            title = request.title,
                            track2 = request.track2,
                            pan = request.pan,
                        ),
                    )
                },
                onExitClick = {
                    onExitClick()
                },
            )
        }
        composable(AppRoutes.REPORT) {
            ReportNavHost { navController.popBackStackIfAvailable() }
        }
    }
}
