package com.danesh.support.navigation



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
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS


import com.danesh.support.GetPinViewModel

import com.danesh.support.R
import com.danesh.support.SuccessSupportResultScreen

import com.danesh.support.SupportScreen
import com.danesh.support.UnSuccessSupportResultScreen


const val TIME_TO_FINISH_SUCCESS_RESULT = RESULT_AUTO_HOME_DELAY_MS

private object SupportRoutes {

    const val LIST = "support_list"

    const val SWIPE_CARD =

        "support_swipe_card/{${SupportNavArgs.AMOUNT}}/{${SupportNavArgs.SERVICE_ID}}/{${SupportNavArgs.TITLE}}"

    const val GET_PIN =

        "support_get_pin/{${SwipeCardNavArgs.TRACK_2}}/{${SupportNavArgs.AMOUNT}}/{${SwipeCardNavArgs.PAN}}/{${SupportNavArgs.SERVICE_ID}}/{${SupportNavArgs.TITLE}}"

    const val SUCCESS_RESULT = "support_success_result/{${SupportNavArgs.RESPONSE}}"

    const val UNSUCCESS_RESULT = "support_unsuccess_result/{${SupportNavArgs.RESPONSE}}"



    fun swipeCard(amount: String, serviceId: String, title: String): String =

        "support_swipe_card/${Uri.encode(amount)}/${Uri.encode(serviceId)}/${Uri.encode(title)}"



    fun getPin(track2: String, amount: String, pan: String, serviceId: String, title: String): String =

        "support_get_pin/${Uri.encode(track2)}/${Uri.encode(amount)}/$pan/${Uri.encode(serviceId)}/${Uri.encode(title)}"



    fun successResult(response: String): String =

        "support_success_result/${Uri.encode(response)}"



    fun unsuccessResult(response: String): String =

        "support_unsuccess_result/${Uri.encode(response)}"

}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SupportNavHost(
    onFlowComplete: () -> Unit,
    directLaunch: SupportDirectLaunch? = null,
) {
    val navController = rememberNavController()
    val resultAutoFinishDelayMs = TIME_TO_FINISH_SUCCESS_RESULT
    val startDestination = directLaunch?.let { launch ->
        val track2 = launch.track2
        val pan = launch.pan
        if (track2.isNotBlank() && pan.isNotBlank()) {
            SupportRoutes.getPin(
                track2 = track2,
                amount = launch.amount,
                pan = pan,
                serviceId = launch.serviceId,
                title = launch.title,
            )
        } else {
            SupportRoutes.swipeCard(
                amount = launch.amount,
                serviceId = launch.serviceId,
                title = launch.title,
            )
        }
    } ?: SupportRoutes.LIST

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {

        composable(SupportRoutes.LIST) {

            SupportScreen(

                viewModel = hiltViewModel(),

                onBackClick = onFlowComplete,

                onConfirmClick = { serviceId, amount, title ->

                    navController.navigate(

                        SupportRoutes.swipeCard(

                            amount = amount,

                            serviceId = serviceId,

                            title = title,

                        ),

                    )

                },

            )

        }

        composable(

            route = SupportRoutes.SWIPE_CARD,

            arguments = listOf(

                navArgument(SupportNavArgs.AMOUNT) { type = NavType.StringType },

                navArgument(SupportNavArgs.SERVICE_ID) { type = NavType.StringType },

                navArgument(SupportNavArgs.TITLE) { type = NavType.StringType },

            ),

        ) { backStackEntry ->

            val amount = backStackEntry.arguments?.getString(SupportNavArgs.AMOUNT).orEmpty()

            val serviceId = backStackEntry.arguments?.getString(SupportNavArgs.SERVICE_ID).orEmpty()

            val title = backStackEntry.arguments?.getString(SupportNavArgs.TITLE).orEmpty()

            SwipeCardScreen(

                viewModel = hiltViewModel(),

                onBackClick = onFlowComplete,

                onCardRead = { track2, pan ->

                    navController.navigate(

                        SupportRoutes.getPin(

                            track2 = track2,

                            amount = amount,

                            pan = pan,

                            serviceId = serviceId,

                            title = title,

                        ),

                    )

                },

                onTimeout = onFlowComplete,

                cancelReading = { onFlowComplete() },

            )

        }

        composable(

            route = SupportRoutes.GET_PIN,

            arguments = listOf(

                navArgument(SwipeCardNavArgs.TRACK_2) { type = NavType.StringType },

                navArgument(SupportNavArgs.AMOUNT) { type = NavType.StringType },

                navArgument(SwipeCardNavArgs.PAN) { type = NavType.StringType },

                navArgument(SupportNavArgs.SERVICE_ID) { type = NavType.StringType },

                navArgument(SupportNavArgs.TITLE) { type = NavType.StringType },

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

                    navController.navigate(SupportRoutes.unsuccessResult(response))

                },

                onSuccessResult = { response ->

                    navController.navigate(SupportRoutes.successResult(response))

                }
            )

        }

        composable(

            route = SupportRoutes.SUCCESS_RESULT,

            arguments = listOf(

                navArgument(SupportNavArgs.RESPONSE) { type = NavType.StringType },

            ),

        ) { backStackEntry ->

            val response = Uri.decode(

                backStackEntry.arguments?.getString(SupportNavArgs.RESPONSE).orEmpty(),

            )

            SuccessSupportResultScreen(

                viewModel = hiltViewModel(),

                response = response,

                onBackClick = onFlowComplete,

                onHomeClick = onFlowComplete,

                autoFinishDelayMs = resultAutoFinishDelayMs,

                successMessageRes = R.string.support_transaction_was_successful,

                transactionTitleRes = R.string.support_transaction_title,

            )

        }

        composable(

            route = SupportRoutes.UNSUCCESS_RESULT,

            arguments = listOf(

                navArgument(SupportNavArgs.RESPONSE) { type = NavType.StringType },

            ),

        ) { backStackEntry ->

            val response = Uri.decode(

                backStackEntry.arguments?.getString(SupportNavArgs.RESPONSE).orEmpty(),

            )

            UnSuccessSupportResultScreen (

                viewModel = hiltViewModel(),

                response = response,

                onBackClick = onFlowComplete,

                onHomeClick = onFlowComplete

            )

        }

    }

}


