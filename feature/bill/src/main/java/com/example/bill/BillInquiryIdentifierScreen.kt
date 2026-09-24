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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.textinput.TransactionField
import com.danesh.ui.theme.AppColors
import com.danesh.ui.toolbar.Toolbar
import com.example.bill.presentation.viewmodel.BillInquiryIdentifierUiState
import com.example.bill.presentation.viewmodel.BillInquiryIdentifierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillInquiryIdentifierScreen(
    viewModel: BillInquiryIdentifierViewModel,
    onBackClick: () -> Unit,
    onContinue: (billId: String, payId: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BillInquiryIdentifierContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onIdentifierChange = viewModel::onIdentifierChange,
        onContinue = { viewModel.validateAndProceed(onContinue) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillInquiryIdentifierContent(
    uiState: BillInquiryIdentifierUiState,
    onBackClick: () -> Unit,
    onIdentifierChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val kind = uiState.kind
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.ScreenBackground,
        topBar = {
            Toolbar(stringResource(kind.titleRes())) { onBackClick() }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = stringResource(kind.promptRes()),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                color = Color(0xFFFFFFFF),
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodySmall,
            )
            TransactionField(
                label = stringResource(kind.fieldLabelRes()),
                value = uiState.identifier,
                onValueChange = onIdentifierChange,
                placeholder = stringResource(kind.placeholderRes()),
                iconRes = kind.iconRes(),
                keyboardType = KeyboardType.Number,
                errorMessage = uiState.identifierError,
            )
            Spacer(modifier = Modifier.weight(1f))
            GradientActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                text = stringResource(R.string.bill_inquiry_continue),
                onClick = onContinue,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
