package com.danesh.card_to_card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.card_to_card.model.CardToCardInput
import com.danesh.card_to_card.model.TransferDestinationType
import com.danesh.card_to_card.presentation.viewmodel.CardToCardTransferUiState
import com.danesh.card_to_card.presentation.viewmodel.CardToCardTransferViewModel
import com.danesh.card_to_card.ui.theme.CardToCardColors
import com.danesh.common.currency.amountFieldLabel
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.textinput.AmountTransactionField
import com.danesh.ui.textinput.TransactionField
import com.danesh.ui.textinput.cardNumberFilter
import com.danesh.ui.toolbar.Toolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardToCardTransferScreen(
    viewModel: CardToCardTransferViewModel,
    onBackClick: () -> Unit = {},
    onConfirmClick: (CardToCardInput) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CardToCardTransferContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onDestinationTypeChange = viewModel::onDestinationTypeChange,
        onDestinationChange = viewModel::onDestinationChange,
        onAmountChange = viewModel::onAmountChange,
        onConfirmClick = { viewModel.validateAndProceed(onConfirmClick) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardToCardTransferContent(
    uiState: CardToCardTransferUiState,
    onBackClick: () -> Unit,
    onDestinationTypeChange: (TransferDestinationType) -> Unit,
    onDestinationChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
) {
    val isWallet = uiState.destinationType == TransferDestinationType.WALLET
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CardToCardColors.Background,
        topBar = {
            Toolbar(stringResource(R.string.card_to_card_title)) { onBackClick() }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.supportsWallet) {
                DestinationTypeSelector(
                    selected = uiState.destinationType,
                    onSelected = onDestinationTypeChange,
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            TransactionField(
                label = stringResource(
                    if (isWallet) R.string.card_to_card_wallet_label
                    else R.string.card_to_card_card_label,
                ),
                value = uiState.destination,
                onValueChange = onDestinationChange,
                placeholder = stringResource(
                    if (isWallet) R.string.card_to_card_wallet_placeholder
                    else R.string.card_to_card_card_placeholder,
                ),
                iconRes = R.drawable.ic_card,
                keyboardType = KeyboardType.Number,
                errorMessage = uiState.destinationError,
                visualTransformation = if (isWallet) {
                    VisualTransformation.None
                } else {
                    VisualTransformation { cardNumberFilter(it.text) }
                },
            )
            Spacer(modifier = Modifier.height(20.dp))
            AmountTransactionField(
                label = amountFieldLabel(),
                value = uiState.amountText,
                onValueChange = onAmountChange,
                placeholder = stringResource(R.string.card_to_card_amount_placeholder),
                iconRes = com.danesh.ui.R.drawable.ic_coin,
                keyboardType = KeyboardType.Number,
                errorMessage = uiState.amountError,
            )
            Spacer(modifier = Modifier.weight(1f))
            GradientActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                text = stringResource(R.string.card_to_card_continue),
                onClick = onConfirmClick,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DestinationTypeSelector(
    selected: TransferDestinationType,
    onSelected: (TransferDestinationType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A3A47))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DestinationChip(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.card_to_card_dest_card),
            selected = selected == TransferDestinationType.CARD,
            onClick = { onSelected(TransferDestinationType.CARD) },
        )
        DestinationChip(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.card_to_card_dest_wallet),
            selected = selected == TransferDestinationType.WALLET,
            onClick = { onSelected(TransferDestinationType.WALLET) },
        )
    }
}

@Composable
private fun DestinationChip(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF1FA2A8) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun CardToCardTransferScreenPreview() {
    CardToCardTransferContent(
        uiState = CardToCardTransferUiState(supportsWallet = true),
        onBackClick = {},
        onDestinationTypeChange = {},
        onDestinationChange = {},
        onAmountChange = {},
        onConfirmClick = {},
    )
}
