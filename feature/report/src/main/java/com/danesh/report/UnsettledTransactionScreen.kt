package com.danesh.report

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.receipt.PrintErrorDialog
import com.danesh.common.receipt.ReceiptType
import com.danesh.common.receipt.ReceiptUi
import com.danesh.common.receipt.TransactionPaperReceipt
import com.danesh.report.ui.ReportEmptyStateScreen
import com.danesh.report.ui.theme.ReportColors

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UnsettledTransactionScreen(
    onFinished: () -> Unit,
    viewModel: UnsettledTransactionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notFoundMessage = stringResource(R.string.report_transaction_not_found)
    val printFailedMessage = stringResource(R.string.report_print_failed)
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingPrint by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.reset()
        viewModel.start(notFoundMessage)
    }

    val currentTransaction = uiState.currentTransaction

    LaunchedEffect(uiState.currentIndex) {
        receiptBitmap = null
        pendingPrint = false
    }

    if (
        currentTransaction != null &&
        uiState.phase == UnsettledTransactionPhase.ReadyToPrint
    ) {
        ReceiptUi(
            receiptKey = "${uiState.currentIndex}-${currentTransaction.trace}-${currentTransaction.date}-${currentTransaction.time}",
            content = {
                TransactionPaperReceipt(
                    result = currentTransaction,
                    receiptType = ReceiptType.UNSETTLED_TRANSACTION,
                )
            },
            onGenerateReceipt = { bitmap ->
                receiptBitmap = bitmap
                pendingPrint = true
            },
        )
    }

    LaunchedEffect(pendingPrint, receiptBitmap, uiState.currentIndex, uiState.phase) {
        if (!pendingPrint || receiptBitmap == null) return@LaunchedEffect
        if (uiState.phase != UnsettledTransactionPhase.ReadyToPrint) return@LaunchedEffect
        val bitmap = receiptBitmap ?: return@LaunchedEffect
        pendingPrint = false
        receiptBitmap = null
        viewModel.printWithBitmap(context, bitmap, printFailedMessage)
    }

    LaunchedEffect(uiState.phase) {
        if (uiState.phase == UnsettledTransactionPhase.Complete) {
            onFinished()
        }
    }

    when (uiState.phase) {
        UnsettledTransactionPhase.Loading,
        UnsettledTransactionPhase.Printing,
        -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF022631)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ReportColors.Accent)
            }
        }

        UnsettledTransactionPhase.ReadyToPrint -> {
            val transaction = currentTransaction
            if (transaction != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TransactionPaperReceipt(
                            result = transaction,
                            receiptType = ReceiptType.UNSETTLED_TRANSACTION,
                            isPaperReceipt = true,
                        )
                    }
                }
            }
        }

        UnsettledTransactionPhase.PrintFailed -> {
            PrintErrorDialog(
                message = uiState.errorMessage.ifBlank { notFoundMessage },
                onDismiss = onFinished,
            )
        }

        UnsettledTransactionPhase.NotFound -> {
            ReportEmptyStateScreen(
                title = stringResource(R.string.report_unsettled),
                message = uiState.errorMessage.ifBlank { notFoundMessage },
                onRetryClick = {
                    viewModel.reset()
                    viewModel.start(notFoundMessage)
                },
                onCancelClick = onFinished,
            )
        }

        UnsettledTransactionPhase.Complete -> Unit
    }
}
