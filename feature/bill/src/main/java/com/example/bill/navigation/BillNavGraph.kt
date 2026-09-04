package com.example.bill.navigation

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
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.SwipeCardScreen
import com.danesh.common.pin.GetPinScreen
import com.example.bill.BillInfoScreen
import com.example.bill.BillInquiryRoute
import com.example.bill.GetPinViewModel
import com.example.bill.SuccessBillResultScreen
import com.example.bill.UnSuccessBillResultScreen
import com.google.gson.Gson

private object BillRoutes {
    const val PURPOSE_INQUIRY = "inquiry"
    const val PURPOSE_PAYMENT = "payment"

    const val BILL_INFO = "bill_info"
    const val SWIPE_CARD =
        "bill_swipe_card/{${BillNavArgs.BILL_ID}}/{${BillNavArgs.PAY_ID}}/{${BillNavArgs.AMOUNT}}/{${BillNavArgs.REQUEST_ID}}/{${BillNavArgs.SWIPE_PURPOSE}}"
    const val INQUIRY =
        "bill_inquiry/{${BillNavArgs.BILL_ID}}/{${BillNavArgs.PAY_ID}}/{${SwipeCardNavArgs.PAN}}/{${SwipeCardNavArgs.TRACK_2}}"
    const val GET_PIN =
        "bill_get_pin/{${SwipeCardNavArgs.TRACK_2}}/{${BillNavArgs.AMOUNT}}/{${SwipeCardNavArgs.PAN}}/{${BillNavArgs.BILL_ID}}/{${BillNavArgs.PAY_ID}}/{${BillNavArgs.REQUEST_ID}}"
    const val SUCCESS_RESULT = "bill_success_result/{${BillNavArgs.RESPONSE}}"
    const val UNSUCCESS_RESULT = "bill_unsuccess_result/{${BillNavArgs.RESPONSE}}"

    fun swipeCard(
        billId: String,
        paymentId: String,
        amount: String,
        requestId: String = "",
        purpose: String = PURPOSE_PAYMENT,
    ): String =
        "bill_swipe_card/${Uri.encode(billId)}/${Uri.encode(paymentId)}/" +
            "${Uri.encode(amount)}/${Uri.encode(requestId)}/${Uri.encode(purpose)}"

    fun inquiry(billId: String, paymentId: String, pan: String, track2: String): String =
        "bill_inquiry/${Uri.encode(billId)}/${Uri.encode(paymentId)}/" +
            "${Uri.encode(pan)}/${Uri.encode(track2)}"

    fun getPin(
        track2: String,
        amount: String,
        pan: String,
        billId: String,
        paymentId: String,
        requestId: String = "",
    ): String =
        "bill_get_pin/${Uri.encode(track2)}/${Uri.encode(amount)}/${Uri.encode(pan)}/" +
            "${Uri.encode(billId)}/${Uri.encode(paymentId)}/${Uri.encode(requestId)}"

    fun successResult(response: String): String =
        "bill_success_result/${Uri.encode(response)}"

    fun unsuccessResult(response: String): String =
        "bill_unsuccess_result/${Uri.encode(response)}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BillNavHost(onFlowComplete: () -> Unit) {
    val navController = rememberNavController()
    val flowRouter: BillFlowRouterViewModel = hiltViewModel()

    fun navigateAfterBillInfo(billId: String, paymentId: String, amount: String) {
        if (flowRouter.requiresInquiry) {
            navController.navigate(
                BillRoutes.swipeCard(
                    billId = billId,
                    paymentId = paymentId,
                    amount = "0",
                    purpose = BillRoutes.PURPOSE_INQUIRY,
                ),
            )
        } else {
            navController.navigate(
                BillRoutes.swipeCard(
                    billId = billId,
                    paymentId = paymentId,
                    amount = amount,
                    purpose = BillRoutes.PURPOSE_PAYMENT,
                ),
            )
        }
    }

    NavHost(
        navController = navController,
        startDestination = BillRoutes.BILL_INFO,
    ) {
        composable(BillRoutes.BILL_INFO) {
            BillInfoScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onShowQrCode = { billId, paymentId, amount ->
                    navigateAfterBillInfo(billId, paymentId, amount)
                },
                onPayWithCard = { billId, paymentId, amount ->
                    navigateAfterBillInfo(billId, paymentId, amount)
                },
            )
        }
        composable(
            route = BillRoutes.SWIPE_CARD,
            arguments = listOf(
                navArgument(BillNavArgs.BILL_ID) { type = NavType.StringType },
                navArgument(BillNavArgs.PAY_ID) { type = NavType.StringType },
                navArgument(BillNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(BillNavArgs.REQUEST_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(BillNavArgs.SWIPE_PURPOSE) {
                    type = NavType.StringType
                    defaultValue = BillRoutes.PURPOSE_PAYMENT
                },
            ),
        ) { backStackEntry ->
            val billId = backStackEntry.arguments?.getString(BillNavArgs.BILL_ID).orEmpty()
            val payId = backStackEntry.arguments?.getString(BillNavArgs.PAY_ID).orEmpty()
            val amount = backStackEntry.arguments?.getString(BillNavArgs.AMOUNT).orEmpty()
            val requestId = backStackEntry.arguments?.getString(BillNavArgs.REQUEST_ID).orEmpty()
            val purpose = backStackEntry.arguments?.getString(BillNavArgs.SWIPE_PURPOSE)
                .orEmpty()
                .ifBlank { BillRoutes.PURPOSE_PAYMENT }
            SwipeCardScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onCardRead = { track2, pan ->
                    if (purpose == BillRoutes.PURPOSE_INQUIRY) {
                        navController.navigate(
                            BillRoutes.inquiry(
                                billId = billId,
                                paymentId = payId,
                                pan = pan,
                                track2 = track2,
                            ),
                        )
                    } else {
                        navController.navigate(
                            BillRoutes.getPin(
                                track2 = track2,
                                amount = amount,
                                pan = pan,
                                billId = billId,
                                paymentId = payId,
                                requestId = requestId,
                            ),
                        )
                    }
                },
                onTimeout = onFlowComplete,
                cancelReading = { onFlowComplete() },
            )
        }
        composable(
            route = BillRoutes.INQUIRY,
            arguments = listOf(
                navArgument(BillNavArgs.BILL_ID) { type = NavType.StringType },
                navArgument(BillNavArgs.PAY_ID) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
            ),
        ) {
            BillInquiryRoute(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onConfirmAndPay = { billId, payId, amount, requestId ->
                    navController.navigate(
                        BillRoutes.swipeCard(
                            billId = billId,
                            paymentId = payId,
                            amount = amount,
                            requestId = requestId,
                            purpose = BillRoutes.PURPOSE_PAYMENT,
                        ),
                    )
                },
                onInquiryFailed = { message ->
                    val payload = Gson().toJson(
                        TransactionResultDetail(
                            isSuccess = false,
                            responseMessage = message,
                            transactionType = TransactionType.BILL,
                        ),
                    )
                    navController.navigate(BillRoutes.unsuccessResult(payload)) {
                        popUpTo(BillRoutes.BILL_INFO) { inclusive = false }
                    }
                },
            )
        }
        composable(
            route = BillRoutes.GET_PIN,
            arguments = listOf(
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
                navArgument(BillNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
                navArgument(BillNavArgs.BILL_ID) { type = NavType.StringType },
                navArgument(BillNavArgs.PAY_ID) { type = NavType.StringType },
                navArgument(BillNavArgs.REQUEST_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
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
                    navController.navigate(BillRoutes.unsuccessResult(response))
                },
                onSuccessResult = { response ->
                    navController.navigate(BillRoutes.successResult(response))
                })
        }
        composable(
            route = BillRoutes.SUCCESS_RESULT,
            arguments = listOf(
                navArgument(BillNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(BillNavArgs.RESPONSE).orEmpty()
            SuccessBillResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = onFlowComplete,
                onHomeClick = onFlowComplete,
            )
        }
        composable(
            route = BillRoutes.UNSUCCESS_RESULT,
            arguments = listOf(
                navArgument(BillNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(BillNavArgs.RESPONSE).orEmpty()
            UnSuccessBillResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = onFlowComplete,
                onHomeClick = onFlowComplete,
                onPrintReceiptClick = {},
            )
        }
    }
}
