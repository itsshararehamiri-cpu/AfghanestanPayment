package com.danesh.wallet_to_wallet

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.TransactionResultDetail
import com.danesh.api.amountRials
import com.danesh.common.AddAmount
import com.danesh.common.AddMerchantIdTerminalId
import com.danesh.common.AddMerchantNamePhone
import com.danesh.common.AddRRNStan
import com.danesh.common.AddReceiptType
import com.danesh.common.AddTypeDateTime
import com.danesh.common.HorizontalDivider
import com.danesh.common.ShowSuccessResult
import com.danesh.common.containerReceiptModifier
import com.danesh.common.locale.titleRes
import com.danesh.common.receipt.AddPSPLog
import com.danesh.common.receipt.AddTransferReceiptDetails
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS
import com.danesh.common.receipt.ReceiptType
import com.danesh.common.receipt.SuccessReceiptPrintHost
import com.danesh.common.receipt.TransactionResultDetailsCard
import com.danesh.common.receipt.rememberSuccessReceiptPrintFlow
import com.danesh.common.result.TransactionResultScreen
import com.danesh.common.rowReceiptModifier
import com.danesh.common.rowReceiptWithPSPLogoModifier
import com.danesh.wallet_to_wallet.presentation.viewmodel.SuccessWalletToWalletViewModel

private const val TIME_TO_FINISH_SUCCESS_RESULT = RESULT_AUTO_HOME_DELAY_MS

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessWalletToWalletResultScreen(
    viewModel: SuccessWalletToWalletViewModel,
    response: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    autoFinishDelayMs: Int = TIME_TO_FINISH_SUCCESS_RESULT,
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
                WalletToWalletReceipt(
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
                        Color(0XFF01242F), Color(0XFF011B28),
                    ),
                ),
            ),
        messageTransaction = stringResource(R.string.wallet_to_wallet_was_successful),
        isSuccess = true,
        onBackClick = { onBackClick() },
    ) {
        if (uiState.result != null) {
            TransactionResultDetailsCard(
                result = uiState.result!!,
                transactionTypeIcon = R.drawable.ic_card,
            )
        }
    }
}

@Composable
private fun WalletToWalletReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType = ReceiptType.CUSTOMER_RECEIPT,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.containerReceiptModifier(true, context),
    ) {
        val firstColor = Black
        val modifierRowReceipt = Modifier.rowReceiptModifier(true)
        AddPSPLog(
            modifier = Modifier.fillMaxWidth(),
            color = firstColor,
            isPaperReceipt = true,
        )
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
            textColor = firstColor,
            isPaperReceipt = true,
        )
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(result.transactionType.titleRes()),
            date = result.date,
            time = result.time,
            textColor = firstColor,
            isPaperReceipt = true,
        )
        HorizontalDivider(isPaperReceipt = true)
        AddMerchantIdTerminalId(
            modifier = modifierRowReceipt,
            merchantId = result.merchantId,
            terminalId = result.terminalId,
            textColor = firstColor,
            isPaperReceipt = true,
        )
        AddTransferReceiptDetails(
            result = result,
            modifier = modifierRowReceipt,
            textColor = firstColor,
            isPaperReceipt = true,
        )
        AddRRNStan(
            modifier = modifierRowReceipt,
            rrn = result.rrn,
            stan = result.trace,
            textColor = firstColor,
            isPaperReceipt = true,
        )
        AddAmount(
            modifier = modifierRowReceipt,
            result.amount,
            isPaperReceipt = true,
            textColor = firstColor,
        )
        ShowSuccessResult(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally),
            firstColor = firstColor,
        )
    }
}
