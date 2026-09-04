package com.danesh.support
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.TransactionResultDetail
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS
import com.danesh.common.receipt.UnSuccessTransactionResultScreen
import com.danesh.support.presentation.UnSuccessSupportViewModel
import com.google.gson.Gson

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnSuccessSupportResultScreen(
    viewModel: UnSuccessSupportViewModel,
    response: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val immediateResult = remember(response) {
        runCatching {
            Gson().fromJson(response, TransactionResultDetail::class.java)
        }.getOrNull()
    }

    LaunchedEffect(response) {
        viewModel.init(response)
    }

    val result = uiState.result ?: immediateResult
    UnSuccessTransactionResultScreen(
        result = result,
        errorInPrint = uiState.errorInPrint,
        failureTitle = stringResource(R.string.support_failed_title),
        transactionTypeIcon = com.danesh.ui.R.drawable.ic_transaction_type,// TODO:
        onClearPrintError = viewModel::clearErrorMessage,
        onPrintFailed = viewModel::setErrorInPrint,
        onPrint = viewModel::print,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        autoFinishDelayMs = RESULT_AUTO_HOME_DELAY_MS,
        showBalanceOnCard = true,
    )
}
