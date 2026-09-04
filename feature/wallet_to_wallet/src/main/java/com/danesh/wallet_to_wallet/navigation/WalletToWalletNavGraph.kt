package com.danesh.wallet_to_wallet.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.currency.currencyLabel
import com.danesh.common.pin.GetPinScreen
import com.danesh.wallet_to_wallet.GetPinViewModel
import com.danesh.wallet_to_wallet.NameInquiryRoute
import com.danesh.wallet_to_wallet.SuccessWalletToWalletResultScreen
import com.danesh.wallet_to_wallet.UnSuccessWalletToWalletResultScreen
import com.danesh.wallet_to_wallet.WalletToWalletConfirmScreen
import com.danesh.wallet_to_wallet.WalletToWalletTransferScreen
import com.danesh.wallet_to_wallet.model.WalletToWalletTransferDetails
import com.danesh.wallet_to_wallet.presentation.viewmodel.NameInquiryViewModel
import com.danesh.wallet_to_wallet.ui.formatAmount
import com.danesh.wallet_to_wallet.ui.walletNumberToDisplay
import com.danesh.common.R as CommonR

private object WalletToWalletRoutes {
    const val TRANSFER = "wallet_to_wallet_transfer"
    const val NAME_INQUIRY =
        "wallet_to_wallet_name_inquiry/{${WalletToWalletNavArgs.SOURCE_WALLET}}/{${WalletToWalletNavArgs.DESTINATION_WALLET}}/{${WalletToWalletNavArgs.AMOUNT}}"
    const val CONFIRM =
        "wallet_to_wallet_confirm/{${WalletToWalletNavArgs.SOURCE_WALLET}}/{${WalletToWalletNavArgs.DESTINATION_WALLET}}/{${WalletToWalletNavArgs.AMOUNT}}/{${WalletToWalletNavArgs.RECIPIENT_NAME}}/{${WalletToWalletNavArgs.RRN}}"
    const val GET_PIN =
        "wallet_to_wallet_get_pin/{${SwipeCardNavArgs.PAN}}/{${WalletToWalletNavArgs.DESTINATION_WALLET}}/{${WalletToWalletNavArgs.AMOUNT}}/{${WalletToWalletNavArgs.RRN}}/{${WalletToWalletNavArgs.RECIPIENT_NAME}}"
    const val SUCCESS_RESULT = "wallet_to_wallet_success_result/{${WalletToWalletNavArgs.RESPONSE}}"
    const val UNSUCCESS_RESULT = "wallet_to_wallet_unsuccess_result/{${WalletToWalletNavArgs.RESPONSE}}"

    fun nameInquiry(
        sourceWallet: String,
        destinationWallet: String,
        amount: String,
    ): String =
        "wallet_to_wallet_name_inquiry/${Uri.encode(sourceWallet)}/${Uri.encode(destinationWallet)}/${Uri.encode(amount)}"

    fun confirm(
        sourceWallet: String,
        destinationWallet: String,
        amount: Int,
        recipientName: String,
        rrn: String,
    ): String =
        "wallet_to_wallet_confirm/${Uri.encode(sourceWallet)}/${Uri.encode(destinationWallet)}/$amount/" +
            "${Uri.encode(recipientName)}/${Uri.encode(rrn)}"

    fun getPin(
        sourceWallet: String,
        destinationWallet: String,
        amount: String,
        rrn: String = "",
        recipientName: String = "",
    ): String =
        "wallet_to_wallet_get_pin/${Uri.encode(sourceWallet)}/${Uri.encode(destinationWallet)}/" +
            "${Uri.encode(amount)}/${Uri.encode(rrn)}/${Uri.encode(recipientName)}"

    fun successResult(response: String): String =
        "wallet_to_wallet_success_result/${Uri.encode(response)}"

    fun unsuccessResult(response: String): String =
        "wallet_to_wallet_unsuccess_result/${Uri.encode(response)}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WalletToWalletNavHost(onFlowComplete: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = WalletToWalletRoutes.TRANSFER,
    ) {
        composable(WalletToWalletRoutes.TRANSFER) {
            WalletToWalletTransferScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onConfirmClick = { input ->
                    navController.navigate(
                        WalletToWalletRoutes.nameInquiry(
                            sourceWallet = input.sourceWallet,
                            destinationWallet = input.destinationWallet,
                            amount = input.amount.toString(),
                        ),
                    )
                },
            )
        }

        composable(
            route = WalletToWalletRoutes.NAME_INQUIRY,
            arguments = listOf(
                navArgument(WalletToWalletNavArgs.SOURCE_WALLET) { type = NavType.StringType },
                navArgument(WalletToWalletNavArgs.DESTINATION_WALLET) { type = NavType.StringType },
                navArgument(WalletToWalletNavArgs.AMOUNT) { type = NavType.StringType },
            ),
        ) {
            NameInquiryRoute(
                viewModel = hiltViewModel<NameInquiryViewModel>(),
                onBackClick = onFlowComplete,
                onInquirySuccess = { holderName, sourceWallet, destinationWallet, amount, rrn ->
                    navController.navigate(
                        WalletToWalletRoutes.confirm(
                            sourceWallet = sourceWallet,
                            destinationWallet = destinationWallet,
                            amount = amount.toIntOrNull() ?: 0,
                            recipientName = holderName,
                            rrn = rrn,
                        ),
                    ) {
                        popUpTo(WalletToWalletRoutes.TRANSFER) { inclusive = false }
                    }
                },
                onInquiryFailed = { responseJson ->
                    navController.navigate(WalletToWalletRoutes.unsuccessResult(responseJson)) {
                        popUpTo(WalletToWalletRoutes.TRANSFER) { inclusive = false }
                    }
                },
            )
        }

        composable(
            route = WalletToWalletRoutes.CONFIRM,
            arguments = listOf(
                navArgument(WalletToWalletNavArgs.SOURCE_WALLET) { type = NavType.StringType },
                navArgument(WalletToWalletNavArgs.DESTINATION_WALLET) { type = NavType.StringType },
                navArgument(WalletToWalletNavArgs.AMOUNT) { type = NavType.IntType },
                navArgument(WalletToWalletNavArgs.RECIPIENT_NAME) { type = NavType.StringType },
                navArgument(WalletToWalletNavArgs.RRN) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val sourceWallet = backStackEntry.arguments
                ?.getString(WalletToWalletNavArgs.SOURCE_WALLET).orEmpty()
            val destinationWallet = backStackEntry.arguments
                ?.getString(WalletToWalletNavArgs.DESTINATION_WALLET).orEmpty()
            val amount = backStackEntry.arguments?.getInt(WalletToWalletNavArgs.AMOUNT) ?: 0
            val rrn = backStackEntry.arguments?.getString(WalletToWalletNavArgs.RRN).orEmpty()
            val holderName = backStackEntry.arguments
                ?.getString(WalletToWalletNavArgs.RECIPIENT_NAME).orEmpty()
            val displayName = holderName.ifBlank {
                stringResource(CommonR.string.placeholder_not_available)
            }
            val details = WalletToWalletTransferDetails(
                recipientName = displayName,
                sourceWalletNumber = walletNumberToDisplay(sourceWallet),
                destinationWalletNumber = walletNumberToDisplay(destinationWallet),
                amount = formatAmount(amount),
                currency = currencyLabel(),
            )

            WalletToWalletConfirmScreen(
                details = details,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.popBackStack(WalletToWalletRoutes.TRANSFER, inclusive = false) },
                onConfirmClick = {
                    navController.navigate(
                        WalletToWalletRoutes.getPin(
                            sourceWallet = sourceWallet,
                            destinationWallet = destinationWallet,
                            amount = amount.toString(),
                            rrn = rrn,
                            recipientName = holderName,
                        ),
                    )
                },
            )
        }

        composable(
            route = WalletToWalletRoutes.GET_PIN,
            arguments = listOf(
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
                navArgument(WalletToWalletNavArgs.DESTINATION_WALLET) { type = NavType.StringType },
                navArgument(WalletToWalletNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(WalletToWalletNavArgs.RRN) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(WalletToWalletNavArgs.RECIPIENT_NAME) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) {
            GetPinScreen(
                viewModel = hiltViewModel<GetPinViewModel>(),
                track2 = "",
                pan = it.arguments?.getString(SwipeCardNavArgs.PAN).orEmpty(),
                onBackClick = onFlowComplete,
                onPinComplete = {},
                onTimeout = onFlowComplete,
                onErrorResult = { response ->
                    navController.navigate(WalletToWalletRoutes.unsuccessResult(response))
                },
                onSuccessResult = { response ->
                    navController.navigate(WalletToWalletRoutes.successResult(response))
                },
            )
        }

        composable(
            route = WalletToWalletRoutes.SUCCESS_RESULT,
            arguments = listOf(
                navArgument(WalletToWalletNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(WalletToWalletNavArgs.RESPONSE).orEmpty()
            SuccessWalletToWalletResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = onFlowComplete,
                onHomeClick = onFlowComplete,
            )
        }

        composable(
            route = WalletToWalletRoutes.UNSUCCESS_RESULT,
            arguments = listOf(
                navArgument(WalletToWalletNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(WalletToWalletNavArgs.RESPONSE).orEmpty()
            UnSuccessWalletToWalletResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = onFlowComplete,
                onHomeClick = onFlowComplete,
                onPrintReceiptClick = {},
            )
        }
    }
}
