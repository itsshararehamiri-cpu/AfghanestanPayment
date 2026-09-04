package com.danesh.wallet_to_wallet

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
import com.danesh.wallet_to_wallet.presentation.viewmodel.NameInquiryViewModel

@Composable
fun NameInquiryRoute(
    viewModel: NameInquiryViewModel,
    onBackClick: () -> Unit,
    onInquirySuccess: (
        holderName: String,
        sourceWallet: String,
        destinationWallet: String,
        amount: String,
        rrn: String,
    ) -> Unit,
    onInquiryFailed: (responseJson: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var failureNavigated by remember { mutableStateOf(false) }
    var successNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.failureResponseJson) {
        val responseJson = uiState.failureResponseJson ?: return@LaunchedEffect
        if (failureNavigated) return@LaunchedEffect
        failureNavigated = true
        onInquiryFailed(responseJson)
    }

    LaunchedEffect(uiState.holderName, uiState.isLoading) {
        if (uiState.isLoading || uiState.holderName.isBlank() || successNavigated) return@LaunchedEffect
        successNavigated = true
        onInquirySuccess(
            uiState.holderName,
            uiState.sourceWallet,
            uiState.destinationWallet,
            uiState.amount,
            uiState.rrn,
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.ScreenBackground,
        topBar = {
            Toolbar(stringResource(R.string.wallet_to_wallet_name_inquiry_title)) { onBackClick() }
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
