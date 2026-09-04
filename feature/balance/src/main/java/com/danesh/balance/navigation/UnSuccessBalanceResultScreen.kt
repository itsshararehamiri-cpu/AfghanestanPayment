package com.danesh.balance.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.TransactionResultDetail
import com.danesh.balance.R
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS
import com.danesh.common.receipt.UnSuccessTransactionResultScreen
import com.google.gson.Gson

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnSuccessBalanceResultScreen(
    viewModel: UnSuccessBalanceResultViewModel,
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
        failureTitle = stringResource(R.string.balance_failed_title),
        transactionTypeIcon = R.drawable.balance,
        onClearPrintError = viewModel::clearErrorMessage,
        onPrintFailed = viewModel::setErrorInPrint,
        onPrint = viewModel::print,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        autoFinishDelayMs = RESULT_AUTO_HOME_DELAY_MS,
        showBalanceOnCard = true,
    )
}
