package com.danesh.wallet_to_wallet

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS
import com.danesh.common.receipt.UnSuccessTransactionResultScreen
import com.danesh.wallet_to_wallet.presentation.viewmodel.UnSuccessWalletToWalletViewModel

private const val TIME_TO_FINISH_SUCCESS_RESULT = RESULT_AUTO_HOME_DELAY_MS

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnSuccessWalletToWalletResultScreen(
    viewModel: UnSuccessWalletToWalletViewModel,
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
        failureTitle = stringResource(R.string.wallet_to_wallet_failed),
        transactionTypeIcon = R.drawable.ic_card,
        onClearPrintError = viewModel::clearErrorMessage,
        onPrintFailed = viewModel::setErrorInPrint,
        onPrint = viewModel::print,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        autoFinishDelayMs = autoFinishDelayMs,
    )
}
