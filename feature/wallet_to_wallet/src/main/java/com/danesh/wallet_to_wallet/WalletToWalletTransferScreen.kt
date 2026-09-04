package com.danesh.wallet_to_wallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.textinput.AmountTransactionField
import com.danesh.ui.textinput.TransactionField
import com.danesh.ui.textinput.cardNumberFilter
import com.danesh.ui.toolbar.Toolbar
import com.danesh.wallet_to_wallet.model.WalletToWalletInput
import com.danesh.wallet_to_wallet.presentation.viewmodel.WalletToWalletTransferUiState
import com.danesh.wallet_to_wallet.presentation.viewmodel.WalletToWalletTransferViewModel
import com.danesh.wallet_to_wallet.ui.theme.WalletToWalletColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletToWalletTransferScreen(
    viewModel: WalletToWalletTransferViewModel,
    onBackClick: () -> Unit = {},
    onConfirmClick: (WalletToWalletInput) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WalletToWalletTransferContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSourceWalletChange = viewModel::onSourceWalletChange,
        onDestinationWalletChange = viewModel::onDestinationWalletChange,
        onAmountChange = viewModel::onAmountChange,
        onConfirmClick = { viewModel.validateAndProceed(onConfirmClick) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletToWalletTransferContent(
    uiState: WalletToWalletTransferUiState,
    onBackClick: () -> Unit,
    onSourceWalletChange: (String) -> Unit,
    onDestinationWalletChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WalletToWalletColors.Background,
        topBar = {
            Toolbar(stringResource(R.string.wallet_to_wallet_title)) { onBackClick() }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            TransactionField(
                label = stringResource(R.string.wallet_to_wallet_source_label),
                value = uiState.sourceWallet,
                onValueChange = onSourceWalletChange,
                placeholder = stringResource(R.string.wallet_to_wallet_source_placeholder),
                iconRes = R.drawable.ic_card,
                keyboardType = KeyboardType.Number,
                errorMessage = uiState.sourceWalletError,
                visualTransformation = VisualTransformation { cardNumberFilter(it.text) },
            )
            Spacer(modifier = Modifier.height(20.dp))
            TransactionField(
                label = stringResource(R.string.wallet_to_wallet_dest_label),
                value = uiState.destinationWallet,
                onValueChange = onDestinationWalletChange,
                placeholder = stringResource(R.string.wallet_to_wallet_dest_placeholder),
                iconRes = R.drawable.ic_card,
                keyboardType = KeyboardType.Number,
                errorMessage = uiState.destinationWalletError,
                visualTransformation = VisualTransformation { cardNumberFilter(it.text) },
            )
            Spacer(modifier = Modifier.height(20.dp))
            AmountTransactionField(
                label = stringResource(R.string.wallet_to_wallet_amount_label),
                value = uiState.amountText,
                onValueChange = onAmountChange,
                placeholder = stringResource(R.string.wallet_to_wallet_amount_placeholder),
                iconRes = com.danesh.ui.R.drawable.ic_coin,
                keyboardType = KeyboardType.Number,
                errorMessage = uiState.amountError,
            )
            Spacer(modifier = Modifier.weight(1f))
            GradientActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                text = stringResource(R.string.wallet_to_wallet_continue),
                onClick = onConfirmClick,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun WalletToWalletTransferScreenPreview() {
    WalletToWalletTransferContent(
        uiState = WalletToWalletTransferUiState(),
        onBackClick = {},
        onSourceWalletChange = {},
        onDestinationWalletChange = {},
        onAmountChange = {},
        onConfirmClick = {},
    )
}
