package com.danesh.common.qr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.danesh.api.TransactionType
import com.danesh.common.locale.displayMerchantName
import com.danesh.common.currency.amountWithCurrency
import com.danesh.common.currency.currencyLabel
import com.danesh.common.locale.titleRes

@Composable
fun QrScanRoute(
    qrContent: String,
    transactionType: TransactionType,
    payableAmount: String,
    onBackClick: () -> Unit,
    onNfcPanelClick: () -> Unit = {},
    viewModel: QrPaymentConfigViewModel = hiltViewModel(),
) {
    val config = remember { viewModel.terminalConfig() }
    val transactionTypeLabel = stringResource(transactionType.titleRes())
    val merchantDisplayName = displayMerchantName(
        config.merchantName.ifBlank { config.merchantId },
        config.englishMerchantName.ifBlank { config.merchantId },
    )

    QrScanScreen(
        qrContent = qrContent,
        details = QrPaymentContent.buildDetails(
            config = config,
            transactionTypeLabel = transactionTypeLabel,
            payableAmount = payableAmount,
            currencyLabel = currencyLabel(),
            merchantDisplayName = merchantDisplayName,
        ),
        onBackClick = onBackClick,
        onNfcPanelClick = onNfcPanelClick,
    )
}

@Composable
fun PurchaseQrScanRoute(
    amount: String,
    onBackClick: () -> Unit,
    onNfcPanelClick: () -> Unit = {},
    viewModel: QrPaymentConfigViewModel = hiltViewModel(),
) {
    val config = remember { viewModel.terminalConfig() }
    QrScanRoute(
        qrContent = QrPaymentContent.buildPurchaseQrContent(config.terminalId, amount),
        transactionType = TransactionType.PURCHASE,
        payableAmount = amount,
        onBackClick = onBackClick,
        onNfcPanelClick = onNfcPanelClick,
        viewModel = viewModel,
    )
}

@Composable
fun BillQrScanRoute(
    billId: String,
    paymentId: String,
    onBackClick: () -> Unit,
    onNfcPanelClick: () -> Unit = {},
    viewModel: QrPaymentConfigViewModel = hiltViewModel(),
) {
    val config = remember { viewModel.terminalConfig() }
    QrScanRoute(
        qrContent = QrPaymentContent.buildBillQrContent(
            terminalId = config.terminalId,
            billId = billId,
            paymentId = paymentId,
        ),
        transactionType = TransactionType.BILL,
        payableAmount = "—",
        onBackClick = onBackClick,
        onNfcPanelClick = onNfcPanelClick,
        viewModel = viewModel,
    )
}
