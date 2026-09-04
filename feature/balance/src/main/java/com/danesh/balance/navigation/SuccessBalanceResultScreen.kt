package com.danesh.balance.navigation

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.TransactionType
import com.danesh.api.amountRials
import com.danesh.common.AddAvailableBalance
import com.danesh.common.AddBalance
import com.danesh.common.AddBalanceFee
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
import com.danesh.common.receipt.balanceTransactionFee
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS
import com.danesh.common.receipt.ReceiptType
import com.danesh.common.receipt.SuccessReceiptPrintHost
import com.danesh.common.receipt.TransactionResultDetailsCard
import com.danesh.common.receipt.rememberSuccessReceiptPrintFlow
import com.danesh.common.receipt.rememberSuccessReceiptPrintFlow
import com.danesh.common.result.TransactionResultScreen
import com.danesh.common.rowReceiptModifier
import com.danesh.common.rowReceiptWithPSPLogoModifier


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessBalanceResultScreen(
    viewModel: SuccessBalanceResultViewModel,
    response: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.init(response)
    }
    val context = LocalContext.current
    val transactionAmountRials = uiState.result?.amountRials()
    val printFlow = rememberSuccessReceiptPrintFlow(
        transactionType = TransactionType.BALANCE,
        transactionAmountRials = transactionAmountRials,
    )
    BackHandler {
        onBackClick()
    }
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }

    if (uiState.result != null) {
        SuccessReceiptPrintHost(
            printFlow = printFlow,
            context = context,
            autoFinishDelayMs = RESULT_AUTO_HOME_DELAY_MS,
            onHomeClick = onHomeClick,
            onCustomerReceiptHandled = viewModel::markCustomerReceiptForQueue,
            onPrint = { bitmap, onSuccess, onFailed ->
//                receiptBitmap=bitmap
                viewModel.print(
                    bitmap = bitmap,
                    context = context,
                    onSuccess = onSuccess,
                    onFailed = onFailed,
                )
            },
            receiptContent = { receiptType ->
                BalanceReceipt(
                    result = uiState.result!!,
                    receiptType = receiptType,
                )
            },
        )
    }
//    if(receiptBitmap!=null){
//        Image(
//            bitmap = receiptBitmap!!.asImageBitmap(),
//            contentDescription = null, modifier = Modifier.wrapContentSize()
//        )
//    }
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
        messageTransaction = stringResource(com.danesh.balance.R.string.balance_success_title),
        isSuccess = true,
        onBackClick = { onBackClick() },
    ) {
        if (uiState.result != null) {
            TransactionResultDetailsCard(
                result = uiState.result!!,
                transactionTypeIcon = com.danesh.balance.R.drawable.balance,
                showBalance = true,
            )
        }
    }


}


@Preview(showBackground = true, backgroundColor = 0xFF001A1A, widthDp = 360, heightDp = 780)
@Composable
private fun TransactionSuccessScreenPreview() {
}

@Composable
private fun BalanceReceipt(
    result: com.danesh.api.TransactionResultDetail,
    receiptType: ReceiptType = ReceiptType.CUSTOMER_RECEIPT,
) {
    val context = LocalContext.current
    val isPaperReceipt = true

    Column(
        modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)
    ) {
        val firstColor = if (isPaperReceipt) Color.Black else MaterialTheme.colorScheme.onSurface
        val modifierRowReceipt = Modifier.rowReceiptModifier(isPaperReceipt)


//        AddHostReceiptText(
//            modifier = modifierRowReceipt,
//            text = result.hostReceiptText,
//            secondText = result.hostReceiptTextSecond,
//            textColor = firstColor,
//            isPaperReceipt = isPaperReceipt,
//        )
        if (isPaperReceipt) {
            AddReceiptType(
                modifier = modifierRowReceipt,
                receiptType = receiptType,
                textColor = firstColor,
            )
        }
        AddMerchantNamePhone(
            modifier = Modifier.rowReceiptWithPSPLogoModifier(isPaperReceipt),
            merchantName = result!!.merchantName,
            merchantPhone = result.merchantPhone,
            englishMerchantName = result.merchantName,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(com.danesh.balance.R.string.balance_),
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
        val actual = result.actualBalance?.takeIf { it.isNotBlank() }
        val available = result.availableBalance?.takeIf { it.isNotBlank() }
        if (actual != null) {
            AddBalance(
                modifier = modifierRowReceipt,
                actual,
                textColor = if (isPaperReceipt) Color.Black else firstColor,
                isPaperReceipt = isPaperReceipt,
            )
        }
        if (available != null && available != actual) {
            AddAvailableBalance(
                modifier = modifierRowReceipt,
                available,
                isPaperReceipt = isPaperReceipt,
                textColor = firstColor,
            )
        } else if (actual == null && available != null) {
            AddAvailableBalance(
                modifier = modifierRowReceipt,
                available,
                isPaperReceipt = isPaperReceipt,
                textColor = firstColor,
            )
        }

        val feeAmount = balanceTransactionFee()
        if (feeAmount.isNotBlank()) {
            AddBalanceFee(
                modifier = modifierRowReceipt
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally),
                fee = feeAmount,
                textColor = firstColor,
                isPaperReceipt = isPaperReceipt,
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

