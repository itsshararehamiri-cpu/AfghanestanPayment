package com.danesh.topup

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
//import com.danesh.topup.model.MobileOperator
import com.danesh.topup.presentation.viewmodel.TopUpUiState
import com.danesh.topup.presentation.viewmodel.TopUpViewModel
import com.danesh.topup.ui.AmountQuickSelectRow
import com.danesh.topup.ui.OperatorSelectionRow
import com.danesh.topup.ui.theme.TopUpColors
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.textinput.TransactionField
import com.danesh.ui.theme.AppColors
import com.danesh.ui.theme.appTextStyle
import com.danesh.ui.toolbar.Toolbar

private val presetAmounts = listOf(5_000, 10_000, 20_000, 50_000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen( viewModel: TopUpViewModel ,
    onBackClick: () -> Unit ,
    onConfirmClick: (mobile: String, operator: MobileOperator, amount: Int) -> Unit ,

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TopUpContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onMobileChange = viewModel::onMobileChange,
        onOperatorSelected = viewModel::onOperatorSelected,
        onAmountChange = viewModel::onAmountChange,
        onPresetAmountSelected = viewModel::onPresetAmountSelected,
        onConfirmClick = { viewModel.validateAndProceed(onConfirmClick) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopUpContent(
    uiState: TopUpUiState,
    onBackClick: () -> Unit,
    onMobileChange: (String) -> Unit,
    onOperatorSelected: (MobileOperator) -> Unit,
    onAmountChange: (String) -> Unit,
    onPresetAmountSelected: (Int) -> Unit,
    onConfirmClick: () -> Unit,
) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = TopUpColors.Background,
            topBar = {
                Toolbar(stringResource(R.string.topup_title)) { onBackClick()}
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
                    label = stringResource(R.string.topup_mobile_label),
                    value = uiState.mobileNumber,
                    onValueChange = onMobileChange,
                    placeholder = stringResource(R.string.topup_mobile_placeholder),
                    iconRes = R.drawable.ic_mobile,
                    keyboardType = KeyboardType.Phone,
                    errorMessage = uiState.mobileError,
                )

                Spacer(modifier = Modifier.height(20.dp))

                OperatorSelectionRow(
                    selectedOperator = uiState.selectedOperator,
                    onOperatorSelected = onOperatorSelected,
                )

                if (uiState.operatorError != null) {
                    Text(
                        text = uiState.operatorError,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text =  stringResource(R.string.topup_amount_placeholder),
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColors.TextOnBackground,
                    textAlign = TextAlign.Start,
                    style = appTextStyle(
                        base = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                AmountQuickSelectRow(
                    presetAmounts = presetAmounts,
                    selectedAmount = uiState.selectedAmount,
                    onAmountSelected = onPresetAmountSelected,
                )
                if (uiState.amountError != null) {
                    Text(
                        text = uiState.amountError,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                GradientActionButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    text = stringResource(R.string.topup_confirm),
                    onClick = onConfirmClick,
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun TopUpScreenPreview() {
    TopUpContent(
        uiState = TopUpUiState(),
        onBackClick = {},
        onMobileChange = {},
        onOperatorSelected = {},
        onAmountChange = {},
        onPresetAmountSelected = {},
        onConfirmClick = {},
    )
}
