package com.danesh.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.report.ui.LastTenSuccessfulTransactionItem
import com.danesh.report.ui.ReportEmptyStateActions
import com.danesh.report.ui.ReportEmptyStateContent
import com.danesh.report.ui.ReportLoadingOverlay
import com.danesh.report.ui.ReportTransactionDetailsHeader
import com.danesh.report.ui.theme.ReportColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastTenSuccessfulTransactionsScreen(
    viewModel: LastTenSuccessfulTransactionsViewModel,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0XFF013543),
                        Color(0XFF043746),
                        Color(0XFF002531),
                    ),
                ),
            ),
    ) {
        ReportTransactionDetailsHeader(
            title = stringResource(R.string.report_last_ten_successful_title),
            onBackClick = onBackClick,
            onFilterClick = null,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                uiState.isLoading && uiState.transactions.isEmpty() -> Unit

                uiState.transactions.isEmpty() -> {
                    ReportEmptyStateContent(
                        message = uiState.error.ifBlank {
                            stringResource(R.string.report_last_ten_successful_empty)
                        },
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = uiState.transactions,
                            key = { item ->
                                "${item.sequenceNumber}-${item.transaction.trace}-${item.transaction.date}-${item.transaction.time}"
                            },
                        ) { item ->
                            LastTenSuccessfulTransactionItem(item = item)
                        }
                        item { Spacer(modifier = Modifier.size(24.dp)) }
                    }
                }
            }

            ReportLoadingOverlay(visible = uiState.isLoading)
        }

        if (uiState.transactions.isEmpty() && !uiState.isLoading) {
            ReportEmptyStateActions(
                onRetryClick = viewModel::loadTransactions,
                onCancelClick = onBackClick,
            )
        }
    }
}
