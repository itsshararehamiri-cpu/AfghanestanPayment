package com.danesh.report

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.TransactionResultDetail
import com.danesh.common.currency.currencyLabel
import com.danesh.common.locale.titleRes
import com.danesh.common.receipt.ElectronicDashedDivider
import com.danesh.common.receipt.ElectronicReceiptAmountRow
import com.danesh.common.receipt.ElectronicReceiptBillIdRow
import com.danesh.common.receipt.ElectronicReceiptCardInfoRow
import com.danesh.common.receipt.ElectronicReceiptDateTimeRow
import com.danesh.common.receipt.ElectronicReceiptDetailRow
import com.danesh.common.receipt.ElectronicReceiptHostTextRow
import com.danesh.common.receipt.ElectronicReceiptMerchantNamePhoneRow
import com.danesh.common.receipt.ElectronicReceiptMerchantTerminalIdRow
import com.danesh.common.receipt.ElectronicReceiptPaymentIdRow
import com.danesh.common.receipt.ElectronicReceiptStanRRnRow
import com.danesh.common.receipt.ElectronicReceiptTransactionTypeResultRow
import com.danesh.common.receipt.ElectronicReceiptVoucherChargeMethodRow
import com.danesh.common.receipt.ElectronicReceiptVoucherPinRow
import com.danesh.common.receipt.ElectronicReceiptVoucherSerialRow
import com.danesh.common.receipt.ReceiptContainer
import com.danesh.common.receipt.ReceiptType
import com.danesh.common.receipt.ReceiptUi
import com.danesh.common.receipt.TransactionPaperReceipt
import com.danesh.common.receipt.formatAmount
import com.danesh.report.ui.ReportEmptyStateScreen
import com.danesh.report.R
import com.danesh.ui.R as UiR
import com.danesh.ui.theme.appScreenBackground

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastTransactionScreen(
    viewModel: LastTransactionViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var receiptBitmap: Bitmap? by remember { mutableStateOf(null) }
    var pendingPrint by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadLastTransaction()
    }

    uiState.result?.let { result ->
        ReceiptUi(content = {
            TransactionPaperReceipt(
                result = result,
                receiptType = ReceiptType.DUPLICATE_RECEIPT,
            )
        }) {
            receiptBitmap = it
        }
    }

    LaunchedEffect(pendingPrint, receiptBitmap) {
        if (!pendingPrint || receiptBitmap == null) return@LaunchedEffect
        val bitmap = receiptBitmap ?: return@LaunchedEffect
        pendingPrint = false
        viewModel.print(
            bitmap = bitmap,
            context = context,
            onSuccess = {},
            onFailed = {},
        )
    }

    if (!uiState.showLoading && uiState.result == null && uiState.error.isNotBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .appScreenBackground(),
        ) {
            ReportEmptyStateScreen(
                title = stringResource(R.string.report_last_transaction),
                message = uiState.error,
                onRetryClick = viewModel::loadLastTransaction,
                onCancelClick = onBackClick,
            )
        }
        return
    }

    ReceiptContainer(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
        toolbarTitle = stringResource(UiR.string.label_back),
        showContent = (uiState.result != null),
        showLoading = uiState.showLoading,
        onBackClick = { onBackClick() },
        errorMessage = uiState.error,
        onHomeClick = {
            onHomeClick()
        },
        printReceiptLabel = stringResource(UiR.string.balance_reprint_receipt),
        onPrintReceiptClick = {
            if (uiState.result != null) {
                pendingPrint = true
            }
        }) {
        val result = uiState.result
        if (result != null) {
            TransactionResultDetailsCard(
                result = result,
                transactionTypeIcon = com.danesh.report.R.drawable.ic_tracking
            )
        }
    }
}

@Composable
private fun TransactionResultDetailsCard(
    result: TransactionResultDetail,
    transactionTypeIcon: Int,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0XFF002E3D),
                        Color(0XFF005562),
                        Color(0XFF002733),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = Color(0xFF1F4955),
                shape = cardShape,
            )
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        result.hostReceiptText?.takeIf { it.isNotBlank() }?.let { hostText ->
            ElectronicReceiptHostTextRow(
                text = hostText,
                secondText = result.hostReceiptTextSecond,
            )
        }
        if (result.hostReceiptText.isNullOrBlank()) {
            result.hostReceiptTextSecond?.takeIf { it.isNotBlank() }?.let { second ->
                ElectronicReceiptHostTextRow(text = second)
            }
        }

        ElectronicReceiptTransactionTypeResultRow(
            type = stringResource(result.transactionType.titleRes()),
            icon = transactionTypeIcon,
            isSuccess = result.isSuccess,
        )

        if (result.merchantName.isNotBlank() || result.englishMerchantName.isNotBlank() || result.merchantPhone.isNotBlank()) {
            ElectronicReceiptMerchantNamePhoneRow(
                merchantName = result.merchantName,
                merchantPhone = result.merchantPhone,
                englishMerchantName = result.englishMerchantName,
            )
        }
        if (result.voucherSerial.isNotBlank()) {
            ElectronicReceiptVoucherSerialRow(
                voucherSerial = result.voucherSerial,
            )
        }
        if (result.voucherPin.isNotBlank()) {
            ElectronicReceiptVoucherPinRow(
                voucherPin = result.voucherPin,
            )
        }
        if (!result.voucherMethod.isNullOrBlank()) {
            ElectronicReceiptVoucherChargeMethodRow(
                operatorCode = result.voucherMethod!!,
            )
        }
        if (result.billId.isNotEmpty()) {
            ElectronicReceiptBillIdRow(
                billId = result.billId,

                )
            ElectronicReceiptPaymentIdRow(
                paymentId = result.payId,

                )
        }

        ElectronicDashedDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0XFF165F73),
        )

        ElectronicReceiptMerchantTerminalIdRow(
            result.terminalId,
            result.merchantId,
        )

        if (result.maskedPan.isNotBlank()) {
            ElectronicReceiptCardInfoRow(
                pan = result.maskedPan,
                issuer = result.issuerName,
            )
        }

        if (result.date.isNotBlank() || result.time.isNotBlank()) {
            ElectronicReceiptDateTimeRow(
                result.date,
                result.time,
            )
        }

        if (result.amount.isNotBlank()) {
            ElectronicReceiptAmountRow(
                result.amount.formatAmount(), currency = currencyLabel()
            )
        }

        ElectronicDashedDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0XFF165F73),
        )

        if (result.trace.isNotBlank() || !result.rrn.isNullOrBlank()) {
            ElectronicReceiptStanRRnRow(
                stan = result.trace,
                rrn = result.rrn,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF001A1A, widthDp = 360, heightDp = 780)
@Composable
private fun TransactionSuccessScreenPreview() {
}
