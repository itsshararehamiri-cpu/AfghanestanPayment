package com.example.bill
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.receipt.UnSuccessTransactionResultScreen
import com.danesh.core.LoggingDevice

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnSuccessBillResultScreen(
    viewModel: UnSuccessBillViewModel,
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
    Log.d("TAG", "UnSuccessBillResultScreen: dddddddd")
    UnSuccessTransactionResultScreen(
        result = uiState.result,
        errorInPrint = uiState.errorInPrint,
        failureTitle = stringResource(R.string.bill_failed),
        transactionTypeIcon =R.drawable.bill,// TODO:
        onClearPrintError = viewModel::clearErrorMessage,
        onPrintFailed = viewModel::setErrorInPrint,
        onPrint = viewModel::print,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        autoFinishDelayMs = autoFinishDelayMs,
    )
}
