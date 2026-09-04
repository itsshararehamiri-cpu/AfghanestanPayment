package com.danesh.cashout.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danesh.cashout.GetPinViewModel
import com.danesh.cashout.SuccessCashOutResultScreen
import com.danesh.cashout.UnSuccessCashOutResultScreen
import com.danesh.cashout.UnSuccessCashOutViewModel
import com.danesh.cashout.presentation.screens.TransactionInfoScreen
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.SwipeCardScreen
import com.danesh.common.pin.GetPinScreen

private object CashOutRoutes {
    const val TRANSACTION_INFO = "cash_out_transaction_info"
    const val SWIPE_CARD = "cash_out_swipe_card/{${CashOutNavArgs.AMOUNT}}"
    const val GET_PIN = "cash_out_get_pin/{${SwipeCardNavArgs.TRACK_2}}/{${CashOutNavArgs.AMOUNT}}/{${SwipeCardNavArgs.PAN}}"

    const val SUCCESS_RESULT = "cash_out_success_result/{${CashOutNavArgs.RESPONSE}}"
    const val UNSUCCESS_RESULT = "cash_out_unsuccess_result/{${CashOutNavArgs.RESPONSE}}"

    fun swipeCard(amount: String): String =
        "cash_out_swipe_card/${Uri.encode(amount)}"

    fun getPin(cardData: String, amount: String,pan: String): String =
        "cash_out_get_pin/${Uri.encode(cardData)}/${Uri.encode(amount)}/$pan"

    fun successResult(response: String): String =
        "cash_out_success_result/${Uri.encode(response)}"

    fun unsuccessResult(response: String): String =
        "cash_out_unsuccess_result/${Uri.encode(response)}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CashOutNavHost(onFlowComplete: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CashOutRoutes.TRANSACTION_INFO,
    ) {
        composable(CashOutRoutes.TRANSACTION_INFO) {
            TransactionInfoScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onPayWithCard = { amount ->
                    navController.navigate(CashOutRoutes.swipeCard(amount))
                },
            )
        }
        composable(
            route = CashOutRoutes.SWIPE_CARD,
            arguments = listOf(
                navArgument(CashOutNavArgs.AMOUNT) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString(CashOutNavArgs.AMOUNT).orEmpty()
            SwipeCardScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onCardRead = { track2,pan ->
                    navController.navigate(CashOutRoutes.getPin(track2, amount,pan))
                },
                onTimeout = onFlowComplete,
                cancelReading = { onFlowComplete() },
            )
        }
        composable(
            route = CashOutRoutes.GET_PIN,
            arguments = listOf(
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
                navArgument(CashOutNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val track2 = backStackEntry.arguments?.getString(SwipeCardNavArgs.TRACK_2).orEmpty()
            val pan = backStackEntry.arguments?.getString(SwipeCardNavArgs.PAN).orEmpty()

            GetPinScreen(
                viewModel = hiltViewModel<GetPinViewModel>(),
                track2 = track2,
                pan=pan,
                onBackClick = onFlowComplete,
                onPinComplete = {},
                onTimeout = onFlowComplete,
                onErrorResult = { response ->
                    navController.navigate(CashOutRoutes.unsuccessResult(response))
                },
                onSuccessResult = { response ->
                    navController.navigate(CashOutRoutes.successResult(response))
                })
        }
        composable(
            route = CashOutRoutes.SUCCESS_RESULT,
            arguments = listOf(
                navArgument(CashOutNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(CashOutNavArgs.RESPONSE).orEmpty()
            SuccessCashOutResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = { onFlowComplete() },
                onHomeClick = onFlowComplete,
                onPrintReceiptClick = {},
            )
        }
        composable(
            route = CashOutRoutes.UNSUCCESS_RESULT,
            arguments = listOf(
                navArgument(CashOutNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(CashOutNavArgs.RESPONSE).orEmpty()
            UnSuccessCashOutResultScreen(
                viewModel = hiltViewModel<UnSuccessCashOutViewModel>(),
                response = response,
                onBackClick = { onFlowComplete() },
                onHomeClick = onFlowComplete,
                onPrintReceiptClick = {},
            )
        }
    }
}
