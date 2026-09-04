package com.danesh.cashdeposit

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.TransactionResultDetail
import com.danesh.api.amountRials
import com.danesh.common.AddAmount
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
import com.danesh.common.receipt.CountdownEffect
import com.danesh.common.receipt.UnSuccessTransactionResultScreen
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS
import com.danesh.common.receipt.ReceiptType
import com.danesh.common.receipt.SuccessReceiptPrintHost
import com.danesh.common.receipt.TransactionResultDetailsCard
import com.danesh.common.receipt.mask
import com.danesh.common.receipt.rememberSuccessReceiptPrintFlow
import com.danesh.common.receipt.rememberSuccessReceiptPrintFlow
import com.danesh.common.result.TransactionResultScreen
import com.danesh.common.rowReceiptModifier
import com.danesh.common.rowReceiptWithPSPLogoModifier

private const val TIME_TO_FINISH_SUCCESS_RESULT = RESULT_AUTO_HOME_DELAY_MS

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessCashDepositResultScreen(
    viewModel: SuccessCashDepositViewModel,
    response: String,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onPrintReceiptClick: () -> Unit = {},
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

    if (uiState.result != null) {
        SuccessReceiptPrintHost(
            printFlow = printFlow,
            context = context,
            autoFinishDelayMs = TIME_TO_FINISH_SUCCESS_RESULT,
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
                ReceiptContent(
                    result = uiState.result!!,
                    receiptType = receiptType,
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
        messageTransaction = stringResource(R.string.cash_deposit_success),
        isSuccess = true,
        onBackClick = { onBackClick() },
    ) {
        uiState.result?.let { result ->
            TransactionResultDetailsCard(
                result = result,
                transactionTypeIcon = R.drawable.cash_deposit,
            )
        }
    }

}

@Composable
private fun CashDepositReceipt(
    result: TransactionResultDetail
) {
    val context = LocalContext.current
    val     isPaperReceipt=true
    Column(
        modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)
    ) {
        val firstColor = if (isPaperReceipt) Black else MaterialTheme.colorScheme.onSurface
        val modifierRowReceipt = Modifier.rowReceiptModifier(isPaperReceipt)
            AddPSPLog(
                modifier = Modifier.fillMaxWidth(),
                color = firstColor,
                isPaperReceipt = false
            )


        AddMerchantNamePhone(
            modifier = Modifier.rowReceiptWithPSPLogoModifier(isPaperReceipt),
            merchantName = result.merchantName,
            merchantPhone = result.merchantPhone,
            englishMerchantName = result.merchantName,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(R.string.cash_deposit_title),
            date = result.date,
            time = result.time,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )
        HorizontalDivider(
            isPaperReceipt = isPaperReceipt
        )
        AddMerchantIdTerminalId(
            modifier = modifierRowReceipt,
            merchantId = result.merchantId,
            terminalId = result.terminalId,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )

        AddMaskedPanCardIssuer(
            modifier = modifierRowReceipt,
            maskedPan = result.maskedPan,
            cardIssuer = result.issuerName,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )
        AddRRNStan(
            modifier = modifierRowReceipt,
            rrn = result.rrn,
            stan = result.trace,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )
        AddAmount(
            modifier = modifierRowReceipt,
            result.availableBalance!!,
            isPaperReceipt = isPaperReceipt,
            textColor = firstColor
        )
//            AddBalance(
//                modifier = modifierRowReceipt,
//                result.realBalance!!,
//                textColor = if (isPaperReceipt) Black else Green60, isPaperReceipt = isPaperReceipt
//            )

//        AddFee(
//            modifier = modifierRowReceipt
//                .wrapContentWidth()
//                .align(Alignment.CenterHorizontally),
//            "1800",
//            firstColor, isPaperReceipt = isPaperReceipt
//        )
//        if (isPaperReceipt) {
//            AddPSPLog(
//                modifier = Modifier.fillMaxWidth(),
//                color = firstColor,
//                isPaperReceipt = true
//            )
//        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnSuccessCashDepositResultScreen(
    viewModel: UnSuccessCashDepositViewModel,
    response: String,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onPrintReceiptClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(response) {
        viewModel.init(response)
    }

    UnSuccessTransactionResultScreen(
        result = uiState.result,
        errorInPrint = uiState.errorInPrint,
        failureTitle = stringResource(R.string.cash_deposit_title),
        transactionTypeIcon = R.drawable.cash_deposit,
        onClearPrintError = viewModel::clearErrorMessage,
        onPrintFailed = viewModel::setErrorInPrint,
        onPrint = viewModel::print,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        autoFinishDelayMs = TIME_TO_FINISH_SUCCESS_RESULT,
    )
}


@Composable
private fun ReceiptContent(
    result: TransactionResultDetail, receiptType: ReceiptType,
) {
    val context = LocalContext.current
    val  isPaperReceipt=true
    Column(
        modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)
    ) {
        val firstColor = if (isPaperReceipt) Black else MaterialTheme.colorScheme.onSurface
            AddReceiptType(
                modifier = Modifier.rowReceiptModifier(isPaperReceipt),
                receiptType = receiptType,
                textColor = firstColor
            )
        AddMerchantNamePhone(
            modifier = Modifier.rowReceiptModifier(isPaperReceipt),
            merchantName = result.merchantName,
            merchantPhone = result.merchantPhone,
            textColor = firstColor,
            englishMerchantName = result.merchantName,// TODO:
            isPaperReceipt = isPaperReceipt
        )
        AddTypeDateTime(
            modifier = Modifier.rowReceiptModifier(isPaperReceipt),
            type = stringResource(R.string.cash_deposit_title),
            date = result.date,
            time = result.time,
            textColor = firstColor,
            isPaperReceipt
        )
        HorizontalDivider(
            isPaperReceipt = isPaperReceipt
        )
        AddMerchantIdTerminalId(
            modifier = Modifier.rowReceiptModifier(isPaperReceipt),
            merchantId = result.merchantId,
            terminalId = result.terminalId,
            textColor = firstColor,
            isPaperReceipt
        )
//        if (!result.posCode.isNullOrEmpty()) AddPosCode(
//            modifier = Modifier.rowReceiptModifier(isPaperReceipt),
//            result.posCode,
//            firstColor,
//            isPaperReceipt
//        )
//        if (!result.purchaseId.isNullOrEmpty()) AddPurchaseId(
//            modifier = Modifier.rowReceiptModifier(isPaperReceipt),
//            purchaseId = result.purchaseId,
//            textColor = firstColor,
//            isPaperReceipt
//        )
        AddMaskedPanCardIssuer(
            modifier = Modifier.rowReceiptModifier(isPaperReceipt),
            maskedPan = result.maskedPan.mask(),
            cardIssuer = result.issuerName,
            textColor = firstColor,
            isPaperReceipt
        )
        AddRRNStan(
            modifier = Modifier.rowReceiptModifier(isPaperReceipt),
            rrn = result.rrn,
            stan = result.trace,
            textColor = firstColor,
            isPaperReceipt
        )
        if (isPaperReceipt)
            AddAmount(
                modifier = Modifier.rowReceiptModifier(isPaperReceipt),
                result.amount, textColor = Black
            )
        if (isPaperReceipt) {
            ShowSuccessResult(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally), firstColor = firstColor
            )
//            if (receiptType == ReceiptType.MERCHANT_RECEIPT)
//                AddCustomerSignature(
//                    modifier = Modifier.rowReceiptModifier(isPaperReceipt),
//                    textColor = firstColor,
//                    isPaperReceipt = true
//                )
//            AddPSPLog(
//                modifier = Modifier.fillMaxWidth(), color = firstColor, isPaperReceipt = true
//            )
        }

    }
}
