package com.danesh.cashdeposit.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.danesh.ui.theme.AppColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.cashdeposit.R
import com.danesh.cashdeposit.presentation.viewmodel.TransactionInfoUiState
import com.danesh.cashdeposit.presentation.viewmodel.TransactionInfoViewModel
import com.danesh.common.currency.amountFieldLabel
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.textinput.AmountTransactionField
import com.danesh.ui.toolbar.Toolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionInfoScreen(
    viewModel: TransactionInfoViewModel,
    onBackClick: () -> Unit,
    onPayWithCard: (amount: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    BackHandler { onBackClick() }
    TransactionInfoContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onAmountChange = viewModel::onAmountChange,
        onPayWithCard = { viewModel.validateAndProceed(context, onPayWithCard) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionInfoContent(
    uiState: TransactionInfoUiState,
    onBackClick: () -> Unit,
    onAmountChange: (String) -> Unit,
    onPayWithCard: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.ScreenBackground,
        topBar = {
            Toolbar(stringResource(R.string.transaction_info)) { onBackClick() }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.plz_enter_transaction_info),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                color = Color(0xFFFFFFFF),
                fontSize = 16.sp,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodySmall,
            )

            AmountTransactionField(
                label = amountFieldLabel(),
                value = uiState.amount,
                onValueChange = onAmountChange,
                placeholder = stringResource(R.string.enter_amount),
                iconRes = com.danesh.ui.R.drawable.ic_coin,
                keyboardType = KeyboardType.Decimal,
                errorMessage = uiState.amountError,
            )

            Spacer(modifier = Modifier.weight(1f))

            GradientActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                text = stringResource(com.danesh.common.R.string.pay_by_card),
                onClick = onPayWithCard,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
