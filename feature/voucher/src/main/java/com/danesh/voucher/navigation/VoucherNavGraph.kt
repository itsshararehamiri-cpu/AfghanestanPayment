package com.danesh.voucher.navigation
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
import com.danesh.voucher.GetPinViewModel
import com.danesh.voucher.SuccessVoucherResultScreen
import com.danesh.voucher.UnSuccessVoucherResultScreen
import com.danesh.voucher.VoucherScreen
import com.danesh.voucher.model.operatorCode

private object VoucherRoutes {
    const val VOUCHER = "voucher"
    const val SWIPE_CARD =
        "voucher_swipe_card/{${VoucherNavArgs.AMOUNT}}/{${VoucherNavArgs.OPERATOR_CODE}}"
    const val GET_PIN =
        "voucher_get_pin/{${SwipeCardNavArgs.TRACK_2}}/{${VoucherNavArgs.AMOUNT}}/{${SwipeCardNavArgs.PAN}}/{${VoucherNavArgs.OPERATOR_CODE}}"
    const val SUCCESS_RESULT = "voucher_success_result/{${VoucherNavArgs.RESPONSE}}"
    const val UNSUCCESS_RESULT = "voucher_unsuccess_result/{${VoucherNavArgs.RESPONSE}}"

    fun swipeCard(amount: String,  operatorCode: String): String =
        "voucher_swipe_card/${Uri.encode(amount)}/${Uri.encode(operatorCode)}"

    fun getPin(track2: String, amount: String, pan: String, operatorCode: String): String =
        "voucher_get_pin/${Uri.encode(track2)}/${Uri.encode(amount)}/$pan/${Uri.encode(operatorCode)}"

    fun successResult(response: String): String =
        "voucher_success_result/${Uri.encode(response)}"

    fun unsuccessResult(response: String): String =
        "voucher_unsuccess_result/${Uri.encode(response)}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VoucherNavHost(onFlowComplete: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = VoucherRoutes.VOUCHER,
    ) {
        composable(VoucherRoutes.VOUCHER) {
            VoucherScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onConfirmClick = {  operator, amount ->
                    navController.navigate(
                        VoucherRoutes.swipeCard(
                            amount = amount.toString(),
                            operatorCode = operator.operatorCode(),
                        ),
                    )
                },
            )
        }
        composable(
            route = VoucherRoutes.SWIPE_CARD,
            arguments = listOf(
                navArgument(VoucherNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(VoucherNavArgs.OPERATOR_CODE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString(VoucherNavArgs.AMOUNT).orEmpty()
            val operatorCode = backStackEntry.arguments?.getString(VoucherNavArgs.OPERATOR_CODE).orEmpty()
            SwipeCardScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onCardRead = { track2, pan ->
                    navController.navigate(
                        VoucherRoutes.getPin(
                            track2 = track2,
                            amount = amount,
                            pan = pan,
                            operatorCode = operatorCode,
                        ),
                    )
                },
                onTimeout = onFlowComplete,
                cancelReading = { onFlowComplete() },
            )
        }
        composable(
            route = VoucherRoutes.GET_PIN,
            arguments = listOf(
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
                navArgument(VoucherNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
                navArgument(VoucherNavArgs.OPERATOR_CODE) { type = NavType.StringType },
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
                    navController.navigate(VoucherRoutes.unsuccessResult(response))
                },
                onSuccessResult = { response ->
                    navController.navigate(VoucherRoutes.successResult(response))
                })
        }
        composable(
            route = VoucherRoutes.SUCCESS_RESULT,
            arguments = listOf(
                navArgument(VoucherNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(VoucherNavArgs.RESPONSE).orEmpty()
            SuccessVoucherResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = onFlowComplete,
                onHomeClick = onFlowComplete,
            )
        }
        composable(
            route = VoucherRoutes.UNSUCCESS_RESULT,
            arguments = listOf(
                navArgument(VoucherNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(VoucherNavArgs.RESPONSE).orEmpty()
            UnSuccessVoucherResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = onFlowComplete,
                onHomeClick = onFlowComplete)
        }
    }
}
