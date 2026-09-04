package com.danesh.card_to_card

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.TransactionType
import com.danesh.card_to_card.presentation.viewmodel.UnSuccessCardTransferViewModel
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS
import com.danesh.common.receipt.UnSuccessTransactionResultScreen

private const val TIME_TO_FINISH_SUCCESS_RESULT = RESULT_AUTO_HOME_DELAY_MS

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnSuccessCardTransferResultScreen(
    viewModel: UnSuccessCardTransferViewModel,
    response: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPrintReceiptClick: () -> Unit = {},
    autoFinishDelayMs: Int = TIME_TO_FINISH_SUCCESS_RESULT,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(response) {
        viewModel.init(response)
    }

    UnSuccessTransactionResultScreen(
        result = uiState.result,
        errorInPrint = uiState.errorInPrint,
        failureTitle = stringResource(
            when (uiState.result?.transactionType) {
                TransactionType.CARD_TO_WALLET -> R.string.card_to_wallet_failed
                else -> R.string.card_to_card_failed
            },
        ),
        transactionTypeIcon = R.drawable.card_to_card,
        onClearPrintError = viewModel::clearErrorMessage,
        onPrintFailed = viewModel::setErrorInPrint,
        onPrint = viewModel::print,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        autoFinishDelayMs = autoFinishDelayMs,
    )
}
