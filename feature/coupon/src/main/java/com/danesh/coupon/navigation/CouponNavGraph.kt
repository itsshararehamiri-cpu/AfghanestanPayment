package com.danesh.coupon.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danesh.common.SwipeCardScreen
import com.danesh.common.pin.GetPinScreen
import com.danesh.coupon.presentation.CouponFlowViewModel
import com.danesh.coupon.presentation.CouponGetPinViewModel
import com.danesh.coupon.ui.CouponCartRoute
import com.danesh.coupon.ui.CouponFailureResultScreen
import com.danesh.coupon.ui.CouponInquiryResultRoute
import com.danesh.coupon.ui.CouponSuccessResultScreen

private object CouponRoutes {
    const val SWIPE_CARD = "coupon_swipe_card"
    const val CART = "coupon_cart"
    const val INQUIRY_RESULT = "coupon_inquiry_result"
    const val GET_PIN = "coupon_get_pin"
    const val SUCCESS = "coupon_success/{response}"
    const val FAILURE = "coupon_failure/{response}"

    fun success(response: String) = "coupon_success/${Uri.encode(response)}"
    fun failure(response: String) = "coupon_failure/${Uri.encode(response)}"
}

/**
 * جریان کالابرگ:
 * کشیدن کارت (بدون رمز) → انتخاب کالا و مبلغ → استعلام → پرداخت با رمز یا لغو استعلام → رسید.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CouponNavHost(onFlowComplete: () -> Unit) {
    val navController = rememberNavController()
    val flowViewModel: CouponFlowViewModel = hiltViewModel()
    LaunchedEffect(Unit) { flowViewModel.start() }
    val finish: () -> Unit = {
        flowViewModel.finish()
        onFlowComplete()
    }

    NavHost(navController = navController, startDestination = CouponRoutes.SWIPE_CARD) {
        composable(CouponRoutes.SWIPE_CARD) {
            SwipeCardScreen(
                viewModel = hiltViewModel(),
                onBackClick = finish,
                onCardRead = { track2, pan ->
                    flowViewModel.onCardRead(track2, pan)
                    navController.navigate(CouponRoutes.CART) {
                        popUpTo(CouponRoutes.SWIPE_CARD) { inclusive = true }
                    }
                },
                onTimeout = finish,
                cancelReading = finish,
            )
        }
        composable(CouponRoutes.CART) {
            CouponCartRoute(
                onBackClick = finish,
                onInquirySucceeded = { navController.navigate(CouponRoutes.INQUIRY_RESULT) },
                onInquiryFailed = { response ->
                    navController.navigate(CouponRoutes.failure(response)) {
                        popUpTo(CouponRoutes.CART) { inclusive = true }
                    }
                },
            )
        }
        composable(CouponRoutes.INQUIRY_RESULT) {
            CouponInquiryResultRoute(
                onPay = { navController.navigate(CouponRoutes.GET_PIN) },
                onFinished = finish,
            )
        }
        composable(CouponRoutes.GET_PIN) {
            GetPinScreen(
                viewModel = hiltViewModel<CouponGetPinViewModel>(),
                track2 = flowViewModel.track2,
                pan = flowViewModel.pan,
                onBackClick = { navController.popBackStack() },
                onPinComplete = { finish() },
                onTimeout = { navController.popBackStack() },
                onSuccessResult = { response ->
                    navController.navigate(CouponRoutes.success(response)) {
                        popUpTo(CouponRoutes.CART) { inclusive = true }
                    }
                },
                onErrorResult = { response ->
                    navController.navigate(CouponRoutes.failure(response)) {
                        popUpTo(CouponRoutes.CART) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = CouponRoutes.SUCCESS,
            arguments = listOf(navArgument("response") { type = NavType.StringType }),
        ) { entry ->
            CouponSuccessResultScreen(
                response = Uri.decode(entry.arguments?.getString("response").orEmpty()),
                onHomeClick = finish,
            )
        }
        composable(
            route = CouponRoutes.FAILURE,
            arguments = listOf(navArgument("response") { type = NavType.StringType }),
        ) { entry ->
            CouponFailureResultScreen(
                response = Uri.decode(entry.arguments?.getString("response").orEmpty()),
                onHomeClick = finish,
            )
        }
    }
}
