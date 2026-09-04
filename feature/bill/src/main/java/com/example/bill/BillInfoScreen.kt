package com.example.bill

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.R as CommonR
import com.danesh.common.currency.amountFieldLabel
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.button.OutlinedActionButton
import com.danesh.ui.textinput.AmountTransactionField
import com.danesh.ui.textinput.TransactionField
import com.danesh.ui.theme.AppColors
import com.danesh.ui.toolbar.Toolbar
import com.example.bill.presentation.viewmodel.BillInfoUiState
import com.example.bill.presentation.viewmodel.BillInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillInfoScreen(
    viewModel: BillInfoViewModel,
    onBackClick: () -> Unit,
    onShowQrCode: (billId: String, paymentId: String, amount: String) -> Unit,
    onPayWithCard: (billId: String, paymentId: String, amount: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BillInfoContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onBillIdChange = viewModel::onBillIdChange,
        onPaymentIdChange = viewModel::onPaymentIdChange,
        onAmountChange = viewModel::onAmountChange,
        onShowQrCode = { viewModel.validateAndProceed(onShowQrCode) },
        onPayWithCard = { viewModel.validateAndProceed(onPayWithCard) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillInfoContent(
    uiState: BillInfoUiState,
    onBackClick: () -> Unit,
    onBillIdChange: (String) -> Unit,
    onPaymentIdChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onShowQrCode: () -> Unit,
    onPayWithCard: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.ScreenBackground,
        topBar = {
            Toolbar(stringResource(CommonR.string.bill_payment_title)) { onBackClick() }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = stringResource(CommonR.string.bill_enter_info),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                color = Color(0XFFFFFFFF),
                fontSize = 16.sp,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodySmall,
            )

            TransactionField(
                label = stringResource(CommonR.string.bill_id_label),
                value = uiState.billId,
                onValueChange = onBillIdChange,
                placeholder = stringResource(CommonR.string.bill_id_placeholder),
                iconRes = R.drawable.ic_bill_id,
                keyboardType = KeyboardType.Decimal,
                errorMessage = uiState.billIdError,
            )

            Spacer(modifier = Modifier.height(20.dp))
                TransactionField(
                    label = stringResource(CommonR.string.payment_id_label),
                    value = uiState.paymentId,
                    onValueChange = onPaymentIdChange,
                    placeholder = stringResource(CommonR.string.payment_id_placeholder),
                    iconRes = R.drawable.ic_bill_id,
                    keyboardType = KeyboardType.Number,
                    errorMessage = uiState.paymentIdError,
                )
            if (uiState.requiresAmountInput) {
                Spacer(modifier = Modifier.height(20.dp))

                AmountTransactionField(
                    label = amountFieldLabel(),
                    value = uiState.amount,
                    onValueChange = onAmountChange,
                    placeholder = stringResource(R.string.enter_amount),
                    iconRes = com.danesh.ui.R.drawable.ic_money_send,
                    errorMessage = uiState.amountError,                 keyboardType = KeyboardType.Decimal
                    )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(12.dp))

            GradientActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                text = stringResource(R.string.bill_confirm_and_pay),
                onClick = onPayWithCard,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF001F24)
@Composable
private fun BillInfoScreenPreview() {
    BillInfoContent(
        uiState = BillInfoUiState(requiresAmountInput = true),
        onBackClick = {},
        onBillIdChange = {},
        onPaymentIdChange = {},
        onAmountChange = {},
        onShowQrCode = {},
        onPayWithCard = {},
    )
}
