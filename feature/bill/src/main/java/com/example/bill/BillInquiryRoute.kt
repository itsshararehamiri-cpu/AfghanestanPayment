package com.example.bill

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.ui.theme.AppColors
import com.danesh.ui.toolbar.Toolbar
import com.example.bill.presentation.viewmodel.BillInquiryViewModel

@Composable
fun BillInquiryRoute(
    viewModel: BillInquiryViewModel,
    onBackClick: () -> Unit,
    onConfirmAndPay: (
        billId: String,
        payId: String,
        amount: String,
        requestId: String,
    ) -> Unit,
    onInquiryFailed: (message: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var failureNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        if (failureNavigated) return@LaunchedEffect
        failureNavigated = true
        onInquiryFailed(message)
    }

    when {
        uiState.isLoading || uiState.errorMessage != null -> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = AppColors.ScreenBackground,
                topBar = {
                    Toolbar(stringResource(R.string.bill_inquiry_result_title)) { onBackClick() }
                },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AppColors.Accent)
                }
            }
        }

        uiState.details != null -> {
            BillInquiryResultScreen(
                details = uiState.details!!,
                onBackClick = onBackClick,
                onConfirmAndPayClick = {
                    onConfirmAndPay(
                        uiState.billId,
                        uiState.payId,
                        uiState.amount,
                        uiState.requestId,
                    )
                },
            )
        }
    }
}
