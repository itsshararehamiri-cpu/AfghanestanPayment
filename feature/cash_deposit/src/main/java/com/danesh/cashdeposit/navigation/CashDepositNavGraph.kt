package com.danesh.cashdeposit.navigation

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
import com.danesh.cashdeposit.GetPinViewModel
import com.danesh.cashdeposit.SuccessCashDepositResultScreen
import com.danesh.cashdeposit.UnSuccessCashDepositResultScreen
import com.danesh.cashdeposit.UnSuccessCashDepositViewModel
import com.danesh.cashdeposit.presentation.screens.TransactionInfoScreen
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.SwipeCardScreen
import com.danesh.common.pin.GetPinScreen

private object CashDepositRoutes {
    const val TRANSACTION_INFO = "cash_deposit_transaction_info"
    const val SWIPE_CARD = "cash_deposit_swipe_card/{${CashDepositNavArgs.AMOUNT}}"
  //  const val GET_PIN = "cash_deposit_get_pin/{${SwipeCardNavArgs.TRACK_2}}/{${CashDepositNavArgs.AMOUNT}}"
    const val GET_PIN = "cash_deposit_get_pin/{${SwipeCardNavArgs.TRACK_2}}/{${CashDepositNavArgs.AMOUNT}}/{${SwipeCardNavArgs.PAN}}"

    const val SUCCESS_RESULT = "cash_deposit_success_result/{${CashDepositNavArgs.RESPONSE}}"
    const val UNSUCCESS_RESULT = "cash_deposit_unsuccess_result/{${CashDepositNavArgs.RESPONSE}}"

    fun swipeCard(amount: String): String =
        "cash_deposit_swipe_card/${Uri.encode(amount)}"

    fun getPin(cardData: String, amount: String,pan: String): String =
        "cash_deposit_get_pin/${Uri.encode(cardData)}/${Uri.encode(amount)}/$pan"

    fun successResult(response: String): String =
        "cash_deposit_success_result/${Uri.encode(response)}"

    fun unsuccessResult(response: String): String =
        "cash_deposit_unsuccess_result/${Uri.encode(response)}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CashDepositNavHost(onFlowComplete: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CashDepositRoutes.TRANSACTION_INFO,
    ) {
        composable(CashDepositRoutes.TRANSACTION_INFO) {
            TransactionInfoScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onPayWithCard = { amount ->
                    navController.navigate(CashDepositRoutes.swipeCard(amount))
                },
            )
        }
        composable(
            route = CashDepositRoutes.SWIPE_CARD,
            arguments = listOf(
                navArgument(CashDepositNavArgs.AMOUNT) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString(CashDepositNavArgs.AMOUNT).orEmpty()
            SwipeCardScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onCardRead = { track2,pan ->
                    navController.navigate(CashDepositRoutes.getPin(track2, amount,pan))
                },
                onTimeout = onFlowComplete,
                cancelReading = { onFlowComplete() },
            )
        }
        composable(
            route = CashDepositRoutes.GET_PIN,
            arguments = listOf(
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
                navArgument(CashDepositNavArgs.AMOUNT) { type = NavType.StringType },
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
                    navController.navigate(CashDepositRoutes.unsuccessResult(response))
                },
                onSuccessResult = { response ->
                    navController.navigate(CashDepositRoutes.successResult(response))
                })
        }
        composable(
            route = CashDepositRoutes.SUCCESS_RESULT,
            arguments = listOf(
                navArgument(CashDepositNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(CashDepositNavArgs.RESPONSE).orEmpty()
            SuccessCashDepositResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = { onFlowComplete() },
                onHomeClick = onFlowComplete,
                onPrintReceiptClick = {},
            )
        }
        composable(
            route = CashDepositRoutes.UNSUCCESS_RESULT,
            arguments = listOf(
                navArgument(CashDepositNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(CashDepositNavArgs.RESPONSE).orEmpty()
            UnSuccessCashDepositResultScreen(
                viewModel = hiltViewModel<UnSuccessCashDepositViewModel>(),
                response = response,
                onBackClick = { onFlowComplete() },
                onHomeClick = onFlowComplete,
                onPrintReceiptClick = {},
            )
        }
    }
}
