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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.common.currency.currencyLabel
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.toolbar.Toolbar
import com.danesh.wallet_to_wallet.model.WalletToWalletTransferDetails
import com.danesh.wallet_to_wallet.ui.WalletToWalletConfirmCard
import com.danesh.wallet_to_wallet.ui.theme.WalletToWalletColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletToWalletConfirmScreen(
    details: WalletToWalletTransferDetails,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onConfirmClick: (WalletToWalletTransferDetails) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WalletToWalletColors.Background,
        topBar = {
            Toolbar(stringResource(R.string.wallet_to_wallet_confirm_title)) {
                onBackClick()
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            WalletToWalletConfirmCard(details = details)
            Spacer(modifier = Modifier.weight(1f))
            GradientActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                text = stringResource(R.string.wallet_to_wallet_confirm),
                onClick = { onConfirmClick(details) },
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun WalletToWalletConfirmScreenPreview() {
    WalletToWalletConfirmScreen(
        details = WalletToWalletTransferDetails(
            recipientName = "Sample Recipient",
            sourceWalletNumber = "5022 2915 5235 5602",
            destinationWalletNumber = "6037 9912 3456 7890",
            amount = "10,000,000",
            currency = currencyLabel(),
        ),
        onConfirmClick = {},
        onBackClick = {},
        onEditClick = {},
    )
}
