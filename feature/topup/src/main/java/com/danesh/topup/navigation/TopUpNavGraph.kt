package com.danesh.topup.navigation

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
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.SwipeCardScreen
import com.danesh.common.pin.GetPinScreen
import com.danesh.topup.GetPinViewModel
import com.danesh.topup.SuccessTopUpResultScreen
import com.danesh.topup.TopUpScreen
import com.danesh.topup.UnSuccessTopUpResultScreen
import com.danesh.topup.operatorCode

private object TopUpRoutes {
    const val TOP_UP = "top_up"
    const val SWIPE_CARD =
        "top_up_swipe_card/{${TopUpNavArgs.AMOUNT}}/{${TopUpNavArgs.MOBILE}}/{${TopUpNavArgs.OPERATOR_CODE}}"
    const val GET_PIN =
        "top_up_get_pin/{${SwipeCardNavArgs.TRACK_2}}/{${TopUpNavArgs.AMOUNT}}/{${SwipeCardNavArgs.PAN}}/{${TopUpNavArgs.MOBILE}}/{${TopUpNavArgs.OPERATOR_CODE}}"
    const val SUCCESS_RESULT = "top_up_success_result/{${TopUpNavArgs.RESPONSE}}"
    const val UNSUCCESS_RESULT = "top_up_unsuccess_result/{${TopUpNavArgs.RESPONSE}}"

    fun swipeCard(amount: String, mobile: String, operatorCode: String): String =
        "top_up_swipe_card/${Uri.encode(amount)}/${Uri.encode(mobile)}/${Uri.encode(operatorCode)}"

    fun getPin(track2: String, amount: String, pan: String, mobile: String, operatorCode: String): String =
        "top_up_get_pin/${Uri.encode(track2)}/${Uri.encode(amount)}/$pan/${Uri.encode(mobile)}/${Uri.encode(operatorCode)}"

    fun successResult(response: String): String =
        "top_up_success_result/${Uri.encode(response)}"

    fun unsuccessResult(response: String): String =
        "top_up_unsuccess_result/${Uri.encode(response)}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TopUpNavHost(onFlowComplete: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = TopUpRoutes.TOP_UP,
    ) {
        composable(TopUpRoutes.TOP_UP) {
            TopUpScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onConfirmClick = { mobile, operator, amount ->
                    navController.navigate(
                        TopUpRoutes.swipeCard(
                            amount = amount.toString(),
                            mobile = mobile,
                            operatorCode = operator.operatorCode(),
                        ),
                    )
                },
            )
        }
        composable(
            route = TopUpRoutes.SWIPE_CARD,
            arguments = listOf(
                navArgument(TopUpNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(TopUpNavArgs.MOBILE) { type = NavType.StringType },
                navArgument(TopUpNavArgs.OPERATOR_CODE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString(TopUpNavArgs.AMOUNT).orEmpty()
            val mobile = backStackEntry.arguments?.getString(TopUpNavArgs.MOBILE).orEmpty()
            val operatorCode = backStackEntry.arguments?.getString(TopUpNavArgs.OPERATOR_CODE).orEmpty()
            SwipeCardScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onCardRead = { track2, pan ->
                    navController.navigate(
                        TopUpRoutes.getPin(
                            track2 = track2,
                            amount = amount,
                            pan = pan,
                            mobile = mobile,
                            operatorCode = operatorCode,
                        ),
                    )
                },
                onTimeout = onFlowComplete,
                cancelReading = { onFlowComplete() },
            )
        }
        composable(
            route = TopUpRoutes.GET_PIN,
            arguments = listOf(
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
                navArgument(TopUpNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
                navArgument(TopUpNavArgs.MOBILE) { type = NavType.StringType },
                navArgument(TopUpNavArgs.OPERATOR_CODE) { type = NavType.StringType },
            ),
        ) {
            GetPinScreen(
                viewModel = hiltViewModel<GetPinViewModel>(),
                track2 = it.arguments?.getString(SwipeCardNavArgs.TRACK_2).orEmpty(),
                pan = it.arguments?.getString(SwipeCardNavArgs.PAN).orEmpty(),
                onBackClick = onFlowComplete,
                onPinComplete = {},
                onTimeout = onFlowComplete,
                onErrorResult = { response ->
                    navController.navigate(TopUpRoutes.unsuccessResult(response))
                },
                onSuccessResult = { response ->
                    navController.navigate(TopUpRoutes.successResult(response))
                })
        }
        composable(
            route = TopUpRoutes.SUCCESS_RESULT,
            arguments = listOf(
                navArgument(TopUpNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(TopUpNavArgs.RESPONSE).orEmpty()
            SuccessTopUpResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = onFlowComplete,
                onHomeClick = onFlowComplete)
        }
        composable(
            route = TopUpRoutes.UNSUCCESS_RESULT,
            arguments = listOf(
                navArgument(TopUpNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(TopUpNavArgs.RESPONSE).orEmpty()
            UnSuccessTopUpResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = onFlowComplete,
                onHomeClick = onFlowComplete)
        }
    }
}
