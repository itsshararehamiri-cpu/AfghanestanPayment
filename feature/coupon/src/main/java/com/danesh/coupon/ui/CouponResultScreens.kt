package com.danesh.coupon.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.amountRials
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS
import com.danesh.common.receipt.SuccessReceiptPrintHost
import com.danesh.common.receipt.TransactionPaperReceipt
import com.danesh.common.receipt.TransactionResultDetailsCard
import com.danesh.common.receipt.UnSuccessTransactionResultScreen
import com.danesh.common.receipt.rememberSuccessReceiptPrintFlow
import com.danesh.common.result.TransactionResultScreen
import com.danesh.coupon.R
import com.danesh.coupon.presentation.CouponResultViewModel

/** رسید موفق خرید کالابرگ: رسید کاغذی شامل سطرهای FC 029 سوئیچ (پرداختی از کالابرگ، مبلغ کل، کد پیگیری). */
@Composable
fun CouponSuccessResultScreen(
    response: String,
    onHomeClick: () -> Unit,
    viewModel: CouponResultViewModel = hiltViewModel(),
) {
    LaunchedEffect(response) { viewModel.init(response) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val result = uiState.result
    val printFlow = rememberSuccessReceiptPrintFlow(transactionAmountRials = result?.amountRials())
    BackHandler { if (printFlow.canNavigateHome()) onHomeClick() }

    if (result != null) {
        SuccessReceiptPrintHost(
            printFlow = printFlow,
            context = context,
            autoFinishDelayMs = RESULT_AUTO_HOME_DELAY_MS,
            onHomeClick = onHomeClick,
            onCustomerReceiptHandled = viewModel::markCustomerReceiptForQueue,
            onPrint = { bitmap, onSuccess, onFailed ->
                viewModel.print(bitmap, context, onSuccess, onFailed)
            },
            receiptContent = { receiptType ->
                TransactionPaperReceipt(result = result, receiptType = receiptType, isPaperReceipt = true)
            },
        )
    }

    TransactionResultScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    listOf(Color(0XFF015455), Color(0XFF012C36), Color(0XFF01242F), Color(0XFF011B28)),
                ),
            ),
        messageTransaction = stringResource(R.string.coupon_purchase_success),
        isSuccess = true,
        onBackClick = { if (printFlow.canNavigateHome()) onHomeClick() },
    ) {
        if (result != null) {
            TransactionResultDetailsCard(result = result, transactionTypeIcon = R.drawable.ic_coupon)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CouponFailureResultScreen(
    response: String,
    onHomeClick: () -> Unit,
    viewModel: CouponResultViewModel = hiltViewModel(),
) {
    LaunchedEffect(response) { viewModel.init(response) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UnSuccessTransactionResultScreen(
        result = uiState.result,
        errorInPrint = uiState.printError,
        failureTitle = stringResource(R.string.coupon_purchase_failed),
        transactionTypeIcon = R.drawable.ic_coupon,
        onClearPrintError = viewModel::clearPrintError,
        onPrintFailed = viewModel::onPrintFailed,
        onPrint = { bitmap, context, onSuccess, onFailed ->
            viewModel.print(bitmap, context, onSuccess, onFailed)
        },
        onBackClick = onHomeClick,
        onHomeClick = onHomeClick,
    )
}
