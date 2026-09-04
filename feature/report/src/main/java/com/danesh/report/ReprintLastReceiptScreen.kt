package com.danesh.report

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.InvalidCardScreen
import com.danesh.common.SwipeCardScreen
import com.danesh.common.presentation.viewmodel.SwipeCardViewModel
import com.danesh.common.receipt.PrintErrorDialog
import com.danesh.common.receipt.ReceiptType
import com.danesh.common.receipt.ReceiptUi
import com.danesh.common.receipt.TransactionPaperReceipt
import com.danesh.report.ui.ReportEmptyStateScreen
import com.danesh.report.ui.theme.ReportColors

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReprintLastReceiptScreen(
    onFinished: () -> Unit,
    viewModel: ReprintLastReceiptViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notFoundMessage = stringResource(R.string.report_transaction_not_found)
    val wrongCardMessage = stringResource(R.string.report_reprint_wrong_card)
    val printFailedMessage = stringResource(R.string.report_print_failed)
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingPrint by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.reset()
        viewModel.start(notFoundMessage)
    }

    uiState.transaction?.let { transaction ->
        if (uiState.phase == ReprintLastReceiptPhase.ReadyToPrint ||
            uiState.phase == ReprintLastReceiptPhase.Printing
        ) {
            ReceiptUi(
                receiptKey = transaction,
                content = {
                    TransactionPaperReceipt(
                        result = transaction,
                        receiptType = ReceiptType.DUPLICATE_RECEIPT,
                    )
                },
                onGenerateReceipt = { bitmap ->
                    receiptBitmap = bitmap
                    pendingPrint = true
                },
            )
        }
    }

    LaunchedEffect(pendingPrint, receiptBitmap) {
        if (!pendingPrint || receiptBitmap == null) return@LaunchedEffect
        val bitmap = receiptBitmap ?: return@LaunchedEffect
        pendingPrint = false
        viewModel.printWithBitmap(context, bitmap, printFailedMessage)
    }

    LaunchedEffect(uiState.phase) {
        if (uiState.phase == ReprintLastReceiptPhase.Complete) {
            onFinished()
        }
    }

    when (uiState.phase) {
        ReprintLastReceiptPhase.Loading,
        ReprintLastReceiptPhase.Printing,
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

        ReprintLastReceiptPhase.AwaitingCard -> {
            val swipeCardViewModel: SwipeCardViewModel = hiltViewModel()
            SwipeCardScreen(
                viewModel = swipeCardViewModel,
                onBackClick = onFinished,
                onCardRead = { _, pan ->
                    viewModel.onCardRead(pan, wrongCardMessage)
                },
                onTimeout = onFinished,
                cancelReading = swipeCardViewModel::clearCardData,
            )
        }

        ReprintLastReceiptPhase.WrongCard -> {
            InvalidCardScreen(
                message = uiState.wrongCardMessage,
                onRetryClick = viewModel::retryCardSwipe,
                onCancelClick = onFinished,
            )
        }

        ReprintLastReceiptPhase.NotFound -> {
            ReportEmptyStateScreen(
                title = stringResource(R.string.report_reprint_last_receipt),
                message = uiState.errorMessage.ifBlank { notFoundMessage },
                onRetryClick = {
                    viewModel.reset()
                    viewModel.start(notFoundMessage)
                },
                onCancelClick = onFinished,
            )
        }

        ReprintLastReceiptPhase.PrintFailed -> {
            PrintErrorDialog(
                message = uiState.errorMessage,
                onDismiss = onFinished,
            )
        }

        ReprintLastReceiptPhase.ReadyToPrint,
        ReprintLastReceiptPhase.Complete,
        -> Unit
    }
}
