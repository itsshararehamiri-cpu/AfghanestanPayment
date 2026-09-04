package com.danesh.support

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.SupportMenuItem
import com.danesh.common.receipt.formatAmount
import com.danesh.support.presentation.SupportUiState
import com.danesh.support.presentation.SupportViewModel
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.theme.AppColors
import com.danesh.ui.toolbar.Toolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    viewModel: SupportViewModel,
    onBackClick: () -> Unit,
    onConfirmClick: (serviceId: String, amount: String, title: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectItemError = stringResource(R.string.support_select_item)
    val invalidAmountError = stringResource(R.string.support_invalid_amount)
    LaunchedEffect(Unit) {
        viewModel.refreshItems()
    }
    SupportContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onItemSelected = viewModel::onItemSelected,
        onConfirmClick = {
            viewModel.validateAndProceed(
                selectItemError = selectItemError,
                invalidAmountError = invalidAmountError,
                onConfirm = onConfirmClick,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportContent(
    uiState: SupportUiState,
    onBackClick: () -> Unit,
    onItemSelected: (SupportMenuItem) -> Unit,
    onConfirmClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.ScreenBackground,
        topBar = {
            Toolbar(stringResource(R.string.support_title)) { onBackClick() }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            if (uiState.items.isEmpty()) {
                Text(
                    text = stringResource(R.string.support_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 24.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp),
                ) {
                    items(uiState.items, key = { it.serviceId + it.title }) { item ->
                        SupportItemRow(
                            item = item,
                            selected = uiState.selected?.serviceId == item.serviceId &&
                                uiState.selected?.title == item.title,
                            onClick = { onItemSelected(item) },
                        )
                    }
                }
            }
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            if (uiState.items.isNotEmpty()) {
                GradientActionButton(
                    modifier = Modifier  .padding(vertical = 16.dp)
                        .fillMaxWidth()
                        .height(52.dp)
                      ,
                    text = stringResource(R.string.support_continue),
                    onClick = onConfirmClick,
                )
            }
        }
    }
}

@Composable
private fun SupportItemRow(
    item: SupportMenuItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = item.amount.formatAmount(),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
