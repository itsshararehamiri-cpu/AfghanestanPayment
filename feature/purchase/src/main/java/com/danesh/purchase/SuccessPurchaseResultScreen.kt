package com.danesh.purchase

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.TransactionResultDetail
import com.danesh.api.amountRials
import com.danesh.common.AddAmount
import com.danesh.common.AddFee
import com.danesh.common.AddMaskedPanCardIssuer
import com.danesh.common.AddMerchantIdTerminalId
import com.danesh.common.AddMerchantNamePhone
import com.danesh.common.AddRRNStan
import com.danesh.common.AddReceiptType
import com.danesh.common.AddTypeDateTime
import com.danesh.common.HorizontalDivider
import com.danesh.common.ShowSuccessResult
import com.danesh.common.containerReceiptModifier
import com.danesh.common.receipt.AddPSPLog
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS
import com.danesh.common.receipt.ReceiptType
import com.danesh.common.receipt.SuccessReceiptPrintHost
import com.danesh.common.receipt.TransactionResultDetailsCard
import com.danesh.common.receipt.receiptPan
import com.danesh.common.receipt.rememberSuccessReceiptPrintFlow
import com.danesh.common.result.TransactionResultScreen
import com.danesh.common.rowReceiptModifier
import com.danesh.common.rowReceiptWithPSPLogoModifier


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessPurchaseResultScreen(
    viewModel: SuccessPurchaseViewModel,
    response: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    autoFinishDelayMs: Int = RESULT_AUTO_HOME_DELAY_MS,
    @StringRes successMessageRes: Int = R.string.purchase_was_successful,
    @StringRes transactionTitleRes: Int = R.string.purchase_title,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.init(response)
    }
    val context = LocalContext.current
    val transactionAmountRials = uiState.result?.amountRials()
    val printFlow = rememberSuccessReceiptPrintFlow(
        transactionAmountRials = transactionAmountRials,
    )
    BackHandler {
        onBackClick()
    }

    if (uiState.result != null) {
        SuccessReceiptPrintHost(
            printFlow = printFlow,
            context = context,
            autoFinishDelayMs = autoFinishDelayMs,
            onHomeClick = onHomeClick,
            onCustomerReceiptHandled = viewModel::markCustomerReceiptForQueue,
            onPrint = { bitmap, onSuccess, onFailed ->
                viewModel.print(
                    bitmap = bitmap,
                    context = context,
                    onSuccess = onSuccess,
                    onFailed = onFailed,
                )
            },
            receiptContent = { receiptType ->
                PurchaseReceipt(
                    result = uiState.result!!,
                    receiptType = receiptType,
                    showFee = uiState.showFeeEnabled,
                    feeAmount = uiState.feeAmount,
                    transactionTitleRes = transactionTitleRes,
                )
            },
        )
    }

    TransactionResultScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0XFF015455), Color(0XFF012C36),
                        Color(0XFF01242F), Color(0XFF011B28)

                    )
                )
            ),
        messageTransaction = stringResource(successMessageRes),
        isSuccess = true,
        onBackClick = { onBackClick() },
    ) {
        if (uiState.result != null) {
            TransactionResultDetailsCard(
                result = uiState.result!!,
                transactionTypeIcon = R.drawable.purchase,
            )
        }
    }

}

@Composable
private fun PurchaseReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType = ReceiptType.CUSTOMER_RECEIPT,
    showFee: Boolean = false,
    feeAmount: String = "",
    @StringRes transactionTitleRes: Int = R.string.purchase_title,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.containerReceiptModifier(true, context)
    ) {
        val firstColor = Black
        val modifierRowReceipt = Modifier.rowReceiptModifier(true)


//        AddHostReceiptText(
//            modifier = modifierRowReceipt,
//            text = result.hostReceiptText,
//            secondText = result.hostReceiptTextSecond,
//            textColor = firstColor,
//            isPaperReceipt = true,
//        )
        AddReceiptType(
            modifier = modifierRowReceipt,
            receiptType = receiptType,
            textColor = firstColor,
        )

        AddMerchantNamePhone(
            modifier = Modifier.rowReceiptWithPSPLogoModifier(true),
            merchantName = result.merchantName,
            merchantPhone = result.merchantPhone,
            englishMerchantName = result.merchantName,
            textColor = firstColor, isPaperReceipt = true
        )
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(transactionTitleRes),
            date = result.date,
            time = result.time,
            textColor = firstColor, isPaperReceipt = true
        )
        HorizontalDivider(
            isPaperReceipt = true
        )
        AddMerchantIdTerminalId(
            modifier = modifierRowReceipt,
            merchantId = result.merchantId,
            terminalId = result.terminalId,
            textColor = firstColor, isPaperReceipt = true
        )

        AddMaskedPanCardIssuer(
            modifier = modifierRowReceipt,
            maskedPan = result.receiptPan(),
            cardIssuer = result.issuerName,
            textColor = firstColor, isPaperReceipt = true
        )
        AddRRNStan(
            modifier = modifierRowReceipt,
            rrn = result.rrn,
            stan = result.trace,
            textColor = firstColor, isPaperReceipt = true
        )
        AddAmount(
            modifier = modifierRowReceipt,
            result.amount,
            isPaperReceipt = true,
            textColor = firstColor
        )
        if (showFee && feeAmount.isNotBlank()) {
            AddFee(
                modifier = modifierRowReceipt
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                fee = feeAmount,
                textColor = firstColor,
                isPaperReceipt = true,
            )
        }
        ShowSuccessResult(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally), firstColor = firstColor
        )
        AddPSPLog(
            modifier = Modifier.fillMaxWidth(),
            color = firstColor,
            isPaperReceipt = true
        )

    }
}

@Preview(showBackground = true, backgroundColor = 0xFF001A1A, widthDp = 360, heightDp = 780)
@Composable
private fun TransactionSuccessScreenPreview() {
}
