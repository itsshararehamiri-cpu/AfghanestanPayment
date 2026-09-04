package com.danesh.balance.navigation

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
import com.danesh.balance.navigation.GetPinViewModel
private const val TAG = "BalanceFlow"

private object BalanceRoutes {
    const val SWIPE_CARD = "balance_swipe_card"
    const val GET_PIN = "balance_get_pin/{${SwipeCardNavArgs.TRACK_2}}/{${SwipeCardNavArgs.PAN}}"
    const val SUCCESS_RESULT = "success_result/{response}"
    const val UNSUCCESS_RESULT = "unsuccess_result/{response}"


    fun getPin(cardData: String, pan: String): String =
        "balance_get_pin/${Uri.encode(cardData)}/$pan"

    fun successResult(response: String): String =
        "success_result/${Uri.encode(response)}"

    fun unsuccessResult(response: String): String =
        "unsuccess_result/${Uri.encode(response)}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BalanceNavHost(onFlowComplete: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BalanceRoutes.SWIPE_CARD,
    ) {
        composable(BalanceRoutes.SWIPE_CARD) {
            SwipeCardScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onCardRead = { track2, pan ->
                    navController.navigate(BalanceRoutes.getPin(track2, pan))
                },
                onTimeout = onFlowComplete,
                cancelReading = { onFlowComplete() },
                showBalanceTransactionFee = true,
            )
        }
        composable(
            route = BalanceRoutes.GET_PIN,
            arguments = listOf(
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val track2 = backStackEntry.arguments?.getString(SwipeCardNavArgs.TRACK_2).orEmpty()
            val pan = backStackEntry.arguments?.getString(SwipeCardNavArgs.PAN).orEmpty()
            GetPinScreen(
                viewModel = hiltViewModel<GetPinViewModel>(),
                track2 = track2,
                pan = pan,
                onBackClick = { onFlowComplete() },
                onPinComplete = { navController.navigate(BalanceRoutes.SUCCESS_RESULT)
                                },
                onTimeout = onFlowComplete,
                onErrorResult = { response ->
                    Log.d(TAG, "NavGraph | onErrorResult | responseLen=${response.length}")
                    navController.navigate(BalanceRoutes.unsuccessResult(response))
                }, onSuccessResult = { response ->
                    Log.d(TAG, "NavGraph | onSuccessResult | responseLen=${response.length}")
                    navController.navigate(BalanceRoutes.successResult(response))
                }
            )
        }
        composable(
            route = BalanceRoutes.SUCCESS_RESULT,
            arguments = listOf(
                navArgument("response") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = Uri.decode(backStackEntry.arguments?.getString("response") ?: "")
            SuccessBalanceResultScreen(
                viewModel = hiltViewModel(), response,
                onBackClick = { onFlowComplete() },
                onHomeClick = { onFlowComplete() }
            )
        }
        composable(
            route = BalanceRoutes.UNSUCCESS_RESULT,
            arguments = listOf(
                navArgument("response") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = Uri.decode(backStackEntry.arguments?.getString("response") ?: "")
            UnSuccessBalanceResultScreen(
                viewModel = hiltViewModel(), response,
                onBackClick = { onFlowComplete() },
                onHomeClick = {
                    onFlowComplete()
                }
            )
        }
    }
}
