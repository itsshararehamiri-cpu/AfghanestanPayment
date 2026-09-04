package com.danesh.card_to_card.navigation

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
import com.danesh.card_to_card.CardToCardConfirmScreen
import com.danesh.card_to_card.CardToCardTransferScreen
import com.danesh.card_to_card.GetPinViewModel
import com.danesh.card_to_card.NameInquiryRoute
import com.danesh.card_to_card.SuccessCardTransferResultScreen
import com.danesh.card_to_card.UnSuccessCardTransferResultScreen
import com.danesh.card_to_card.model.CardToCardTransferDetails
import com.danesh.card_to_card.model.TransferDestinationType
import com.danesh.card_to_card.ui.cardNumberToDisplay
import com.danesh.card_to_card.ui.formatAmount
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.SwipeCardScreen
import com.danesh.common.currency.currencyLabel
import com.danesh.common.pin.GetPinScreen
import com.danesh.common.R as CommonR

private object CardToCardRoutes {
    const val SWIPE_SOURCE = "card_to_card_swipe_source"
    const val TRANSFER =
        "card_to_card_transfer/{${SwipeCardNavArgs.PAN}}/{${SwipeCardNavArgs.TRACK_2}}"
    const val NAME_INQUIRY =
        "card_to_card_name_inquiry/{${CardToCardNavArgs.DESTINATION}}/{${CardToCardNavArgs.AMOUNT}}/{${CardToCardNavArgs.DEST_TYPE}}/{${SwipeCardNavArgs.PAN}}/{${SwipeCardNavArgs.TRACK_2}}"
    const val CONFIRM =
        "card_to_card_confirm/{${CardToCardNavArgs.DESTINATION}}/{${CardToCardNavArgs.AMOUNT}}/{${CardToCardNavArgs.DEST_TYPE}}/{${CardToCardNavArgs.RECIPIENT_NAME}}/{${CardToCardNavArgs.RRN}}/{${SwipeCardNavArgs.PAN}}/{${SwipeCardNavArgs.TRACK_2}}"
    const val GET_PIN =
        "card_to_card_get_pin/{${SwipeCardNavArgs.TRACK_2}}/{${CardToCardNavArgs.AMOUNT}}/{${SwipeCardNavArgs.PAN}}/{${CardToCardNavArgs.DESTINATION}}/{${CardToCardNavArgs.DEST_TYPE}}/{${CardToCardNavArgs.RRN}}/{${CardToCardNavArgs.RECIPIENT_NAME}}"
    const val SUCCESS_RESULT = "card_to_card_success_result/{${CardToCardNavArgs.RESPONSE}}"
    const val UNSUCCESS_RESULT = "card_to_card_unsuccess_result/{${CardToCardNavArgs.RESPONSE}}"

    fun transfer(pan: String, track2: String): String =
        "card_to_card_transfer/${Uri.encode(pan)}/${Uri.encode(track2)}"

    fun nameInquiry(
        destination: String,
        amount: String,
        destType: String,
        pan: String,
        track2: String,
    ): String =
        "card_to_card_name_inquiry/${Uri.encode(destination)}/${Uri.encode(amount)}/" +
            "${Uri.encode(destType)}/${Uri.encode(pan)}/${Uri.encode(track2)}"

    fun confirm(
        destination: String,
        amount: Int,
        destType: String,
        recipientName: String,
        rrn: String,
        pan: String,
        track2: String,
    ): String =
        "card_to_card_confirm/${Uri.encode(destination)}/$amount/" +
            "${Uri.encode(destType)}/${Uri.encode(recipientName)}/${Uri.encode(rrn)}/" +
            "${Uri.encode(pan)}/${Uri.encode(track2)}"

    fun getPin(
        track2: String,
        amount: String,
        pan: String,
        destination: String,
        destType: String,
        rrn: String = "",
        recipientName: String = "",
    ): String =
        "card_to_card_get_pin/${Uri.encode(track2)}/${Uri.encode(amount)}/" +
            "${Uri.encode(pan)}/${Uri.encode(destination)}/${Uri.encode(destType)}/" +
            "${Uri.encode(rrn)}/${Uri.encode(recipientName)}"

    fun successResult(response: String): String =
        "card_to_card_success_result/${Uri.encode(response)}"

    fun unsuccessResult(response: String): String =
        "card_to_card_unsuccess_result/${Uri.encode(response)}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CardToCardNavHost(onFlowComplete: () -> Unit) {
    val navController = rememberNavController()
    val flowRouter: TransferFlowRouterViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = CardToCardRoutes.SWIPE_SOURCE,
    ) {
        composable(CardToCardRoutes.SWIPE_SOURCE) {
            SwipeCardScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onCardRead = { track2, pan ->
                    navController.navigate(CardToCardRoutes.transfer(pan = pan, track2 = track2))
                },
                onTimeout = onFlowComplete,
                cancelReading = { onFlowComplete() },
            )
        }

        composable(
            route = CardToCardRoutes.TRANSFER,
            arguments = listOf(
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val sourcePan = backStackEntry.arguments?.getString(SwipeCardNavArgs.PAN).orEmpty()
            val sourceTrack2 = backStackEntry.arguments?.getString(SwipeCardNavArgs.TRACK_2).orEmpty()
            CardToCardTransferScreen(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onConfirmClick = { input ->
                    val destType = input.destinationType.name
                    val amount = input.amount.toString()
                    if (flowRouter.requiresNameInquiry) {
                        navController.navigate(
                            CardToCardRoutes.nameInquiry(
                                destination = input.destination,
                                amount = amount,
                                destType = destType,
                                pan = sourcePan,
                                track2 = sourceTrack2,
                            ),
                        )
                    } else {
                        navController.navigate(
                            CardToCardRoutes.confirm(
                                destination = input.destination,
                                amount = input.amount,
                                destType = destType,
                                recipientName = "—",
                                rrn = "",
                                pan = sourcePan,
                                track2 = sourceTrack2,
                            ),
                        )
                    }
                },
            )
        }

        composable(
            route = CardToCardRoutes.NAME_INQUIRY,
            arguments = listOf(
                navArgument(CardToCardNavArgs.DESTINATION) { type = NavType.StringType },
                navArgument(CardToCardNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(CardToCardNavArgs.DEST_TYPE) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val sourcePan = backStackEntry.arguments?.getString(SwipeCardNavArgs.PAN).orEmpty()
            val sourceTrack2 = backStackEntry.arguments?.getString(SwipeCardNavArgs.TRACK_2).orEmpty()
            NameInquiryRoute(
                viewModel = hiltViewModel(),
                onBackClick = onFlowComplete,
                onInquirySuccess = { holderName, destination, amount, destType, rrn ->
                    navController.navigate(
                        CardToCardRoutes.confirm(
                            destination = destination,
                            amount = amount.toIntOrNull() ?: 0,
                            destType = destType,
                            recipientName = holderName,
                            rrn = rrn,
                            pan = sourcePan,
                            track2 = sourceTrack2,
                        ),
                    ) {
                        popUpTo(CardToCardRoutes.TRANSFER) { inclusive = false }
                    }
                },
                onInquiryFailed = { responseJson ->
                    navController.navigate(CardToCardRoutes.unsuccessResult(responseJson)) {
                        popUpTo(CardToCardRoutes.SWIPE_SOURCE) { inclusive = false }
                    }
                },
            )
        }

        composable(
            route = CardToCardRoutes.CONFIRM,
            arguments = listOf(
                navArgument(CardToCardNavArgs.DESTINATION) { type = NavType.StringType },
                navArgument(CardToCardNavArgs.AMOUNT) { type = NavType.IntType },
                navArgument(CardToCardNavArgs.DEST_TYPE) { type = NavType.StringType },
                navArgument(CardToCardNavArgs.RECIPIENT_NAME) { type = NavType.StringType },
                navArgument(CardToCardNavArgs.RRN) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val destination = backStackEntry.arguments?.getString(CardToCardNavArgs.DESTINATION).orEmpty()
            val amount = backStackEntry.arguments?.getInt(CardToCardNavArgs.AMOUNT) ?: 0
            val destType = backStackEntry.arguments?.getString(CardToCardNavArgs.DEST_TYPE).orEmpty()
            val rrn = backStackEntry.arguments?.getString(CardToCardNavArgs.RRN).orEmpty()
            val sourcePan = backStackEntry.arguments?.getString(SwipeCardNavArgs.PAN).orEmpty()
            val sourceTrack2 = backStackEntry.arguments?.getString(SwipeCardNavArgs.TRACK_2).orEmpty()
            val holderName = backStackEntry.arguments
                ?.getString(CardToCardNavArgs.RECIPIENT_NAME)
                .orEmpty()
            val displayName = holderName.ifBlank {
                stringResource(CommonR.string.placeholder_not_available)
            }
            val isWallet = TransferDestinationType.fromNav(destType) == TransferDestinationType.WALLET
            val details = CardToCardTransferDetails(
                recipientName = displayName,
                sourceCardNumber = cardNumberToDisplay(sourcePan),
                destinationNumber = if (isWallet) destination else cardNumberToDisplay(destination),
                destinationIsWallet = isWallet,
                amount = formatAmount(amount),
                currency = currencyLabel(),
            )

            CardToCardConfirmScreen(
                details = details,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.popBackStack(CardToCardRoutes.TRANSFER, inclusive = false) },
                onConfirmClick = {
                    navController.navigate(
                        CardToCardRoutes.getPin(
                            track2 = sourceTrack2,
                            amount = amount.toString(),
                            pan = sourcePan,
                            destination = destination,
                            destType = destType,
                            rrn = rrn,
                            recipientName = holderName,
                        ),
                    )
                },
            )
        }

        composable(
            route = CardToCardRoutes.GET_PIN,
            arguments = listOf(
                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },
                navArgument(CardToCardNavArgs.AMOUNT) { type = NavType.StringType },
                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },
                navArgument(CardToCardNavArgs.DESTINATION) { type = NavType.StringType },
                navArgument(CardToCardNavArgs.DEST_TYPE) { type = NavType.StringType },
                navArgument(CardToCardNavArgs.RRN) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(CardToCardNavArgs.RECIPIENT_NAME) {
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
                    navController.navigate(CardToCardRoutes.unsuccessResult(response))
                },
                onSuccessResult = { response ->
                    navController.navigate(CardToCardRoutes.successResult(response))
                })
        }

        composable(
            route = CardToCardRoutes.SUCCESS_RESULT,
            arguments = listOf(
                navArgument(CardToCardNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(CardToCardNavArgs.RESPONSE).orEmpty()
            SuccessCardTransferResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = onFlowComplete,
                onHomeClick = onFlowComplete,
            )
        }

        composable(
            route = CardToCardRoutes.UNSUCCESS_RESULT,
            arguments = listOf(
                navArgument(CardToCardNavArgs.RESPONSE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString(CardToCardNavArgs.RESPONSE).orEmpty()
            UnSuccessCardTransferResultScreen(
                viewModel = hiltViewModel(),
                response = response,
                onBackClick = onFlowComplete,
                onHomeClick = onFlowComplete,
                onPrintReceiptClick = {},
            )
        }
    }
}
