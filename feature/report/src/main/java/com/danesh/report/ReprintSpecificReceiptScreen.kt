package com.danesh.report

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.receipt.ReceiptType
import com.danesh.common.receipt.ReceiptUi
import com.danesh.common.receipt.TransactionPaperReceipt
import com.danesh.report.ui.ReportEmptyStateScreen
import com.danesh.report.ui.ReportFilterTextField
import com.danesh.report.ui.theme.ReportColors
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.toolbar.Toolbar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReprintSpecificReceiptScreen(
    onFinished: () -> Unit,
    viewModel: ReprintSpecificReceiptViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notFoundMessage = stringResource(R.string.report_transaction_not_found)
    val printFailedMessage = stringResource(R.string.report_print_failed)
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingPrint by remember { mutableStateOf(false) }

    uiState.transaction?.let { transaction ->
        if (uiState.phase == ReprintSpecificReceiptPhase.ReadyToPrint ||
            uiState.phase == ReprintSpecificReceiptPhase.Printing
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
        if (uiState.phase == ReprintSpecificReceiptPhase.Complete) {
            onFinished()
        }
    }

    if (uiState.phase == ReprintSpecificReceiptPhase.NotFound) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF022631)),
        ) {
            ReportEmptyStateScreen(
                title = stringResource(R.string.report_reprint_specific_receipt),
                message = uiState.errorMessage.ifBlank { notFoundMessage },
                onRetryClick = viewModel::dismissNotFound,
                onCancelClick = onFinished,
            )
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF022631)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(
                title = stringResource(R.string.report_reprint_specific_receipt),
                onBackClick = onFinished,
            )

            if (uiState.phase == ReprintSpecificReceiptPhase.Input) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.report_reprint_specific_hint),
                        color = ReportColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ReportFilterTextField(
                        value = uiState.trackingNumber,
                        onValueChange = viewModel::updateTrackingNumber,
                        placeholder = stringResource(R.string.report_filters_tracking_number),
                        iconRes = R.drawable.ic_tracking,
                        keyboardType = KeyboardType.Number,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ReportFilterTextField(
                        value = uiState.referenceNumber,
                        onValueChange = viewModel::updateReferenceNumber,
                        placeholder = stringResource(R.string.report_filters_reference_number),
                        iconRes = R.drawable.ic_reference,
                        keyboardType = KeyboardType.Number,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GradientActionButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        text = stringResource(R.string.report_reprint_specific_print),
                        onClick = { viewModel.submit(notFoundMessage) },
                    )
                }
            }
        }

        if (uiState.phase == ReprintSpecificReceiptPhase.Loading ||
            uiState.phase == ReprintSpecificReceiptPhase.Printing
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ReportColors.Accent)
            }
        }
    }
}
