package com.danesh.purchase.navigation

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.SwipeCardScreen
import com.danesh.common.pin.GetPinScreen
import com.danesh.purchase.GetPinViewModel
import com.danesh.purchase.SuccessPurchaseResultScreen
import com.danesh.purchase.TIME_TO_FINISH_SUCCESS_RESULT
import com.danesh.purchase.UnSuccessBalanceResultScreen
import com.danesh.purchase.navigation.PurchaseRoutes.SUCCESS_RESULT
import com.danesh.purchase.navigation.PurchaseRoutes.UNSUCCESS_RESULT
import com.danesh.purchase.presentation.screens.TransactionInfoScreen

private object PurchaseRoutes {
    const val TRANSACTION_INFO = "purchase_transaction_info"
    const val SWIPE_CARD = "purchase_swipe_card/{${PurchaseNavArgs.AMOUNT}}"
    const val GET_PIN = "purchase_get_pin/{${SwipeCardNavArgs.TRACK_2}}/{${PurchaseNavArgs.AMOUNT}}/{${SwipeCardNavArgs.PAN}}"
    fun swipeCard(amount: String): String =
        "purchase_swipe_card/${Uri.encode(amount)}"

    fun getPin(track2: String, amount: String,pan: String): String =
        "purchase_get_pin/${Uri.encode(track2)}/${Uri.encode(amount)}/$pan"

    const val SUCCESS_RESULT = "success_result/{response}"
    const val UNSUCCESS_RESULT = "unsuccess_result/{response}"

    fun successResult(response: String): String =
        "success_result/${Uri.encode(response)}"

    fun unsuccessResult(response: String): String =
        "unsuccess_result/${Uri.encode(response)}"
}

private const val EXTERNAL_PURCHASE_RESULT_DELAY_MS = 5_000

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PurchaseNavHost(
    onFlowComplete: () -> Unit,
    externalPurchaseAmount: String? = null,
    onExternalPurchaseComplete: ((responseJson: String) -> Unit)? = null,
) {
    val navController = rememberNavController()
    val startDestination = externalPurchaseAmount?.let { PurchaseRoutes.swipeCard(it) }
        ?: PurchaseRoutes.TRANSACTION_INFO

    val completeWithResult: (String) -> Unit = { response ->
        if (onExternalPurchaseComplete != null) {
            onExternalPurchaseComplete(response)
        } else {
            onFlowComplete()
        }
    }
    val resultAutoFinishDelayMs = if (externalPurchaseAmount != null) {
        EXTERNAL_PURCHASE_RESULT_DELAY_MS
    } else {
        TIME_TO_FINISH_SUCCESS_RESULT
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(PurchaseRoutes.TRANSACTION_INFO) {
            TransactionInfoScreen(
                viewModel = hiltViewModel(),
                onBackClick = {
                    onFlowComplete()
                },
                onShowQrCode = { amount ->
                    navController.navigate(PurchaseRoutes.swipeCard(amount))
                },
                onPayWithCard = { amount ->
                    navController.navigate(PurchaseRoutes.swipeCard(amount))
                },
            )
        }
        composable(
            route = PurchaseRoutes.SWIPE_CARD,
            arguments = listOf(
                navArgument(PurchaseNavArgs.AMOUNT) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString(PurchaseNavArgs.AMOUNT).orEmpty()
            SwipeCardScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onCardRead = { track2,pan ->
                    navController.navigate(PurchaseRoutes.getPin(track2, amount,pan))
                },
                onTimeout = onFlowComplete,
                cancelReading = { onFlowComplete() },
            )
        }
        composable(
            route = PurchaseRoutes.GET_PIN,
            arguments = listOf(
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
                navArgument(PurchaseNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val track2 = backStackEntry.arguments?.getString(SwipeCardNavArgs.TRACK_2).orEmpty()
            val pan = backStackEntry.arguments?.getString(SwipeCardNavArgs.PAN).orEmpty()
            GetPinScreen(viewModel = hiltViewModel<GetPinViewModel>(),
                track2 = track2, pan = pan,
                onBackClick = onFlowComplete,
                onPinComplete = { onFlowComplete() },
                onTimeout = onFlowComplete,
                onErrorResult = { response ->
                    navController.navigate(PurchaseRoutes.unsuccessResult(response))
                },
                onSuccessResult = { response ->
                    navController.navigate(PurchaseRoutes.successResult(response))
                })
        }
        composable(
            route = SUCCESS_RESULT,
            arguments = listOf(
                navArgument("response") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = Uri.decode(
                backStackEntry.arguments?.getString("response").orEmpty(),
            )
            SuccessPurchaseResultScreen(viewModel = hiltViewModel(),response,
                onBackClick = { completeWithResult(response) },
                onHomeClick = { completeWithResult(response) },

                autoFinishDelayMs = resultAutoFinishDelayMs,
            )
        }
        composable(
            route = UNSUCCESS_RESULT,
            arguments = listOf(
                navArgument("response") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = Uri.decode(
                backStackEntry.arguments?.getString("response").orEmpty(),
            )
            UnSuccessBalanceResultScreen(viewModel = hiltViewModel(),response,
                onBackClick = { completeWithResult(response) },
                onHomeClick = { completeWithResult(response) },
                onPrintReceiptClick = {},
                autoFinishDelayMs = resultAutoFinishDelayMs,
            )
        }
    }
}
