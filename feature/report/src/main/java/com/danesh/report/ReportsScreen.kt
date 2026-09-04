package com.danesh.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.report.model.ReportSummary
import com.danesh.report.ui.ReportLoadingOverlay
import com.danesh.report.ui.ReportMenuRow
import com.danesh.report.ui.ReportSummaryCard
import com.danesh.ui.toolbar.Toolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    summary: ReportSummary ,
    isLoading: Boolean = false,
    onBackClick: () -> Unit ,
    onAggregateReportClick: () -> Unit,
    onTransactionDetailsClick: () -> Unit ,
    onLastTransactionClick: () -> Unit,
    onLastTenSuccessfulClick: () -> Unit,
    onUnsettledTransactionsClick: () -> Unit,
    onReprintLastReceiptClick: () -> Unit,
    onReprintSpecificReceiptClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize().background(brush = Brush.radialGradient( listOf(Color(0XFF013543),
                Color(0XFF043746),
                Color(0XFF002531))))
    ) {
        Toolbar(stringResource(R.string.report_title)) { onBackClick()}

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            ReportSummaryCard(summary = summary)
            ReportLoadingOverlay(visible = isLoading)
        }

        Spacer(modifier = Modifier.height(20.dp))

        ReportMenuRow(
            onAggregateReportClick = onAggregateReportClick,
            onTransactionDetailsClick = onTransactionDetailsClick,
            onLastTransactionClick = onLastTransactionClick,
            onLastTenSuccessfulClick = onLastTenSuccessfulClick,
            onUnsettledTransactionsClick = onUnsettledTransactionsClick,
            onReprintLastReceiptClick = onReprintLastReceiptClick,
            onReprintSpecificReceiptClick = onReprintSpecificReceiptClick,
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun ReportsScreenPreview() {
    ReportsScreen(
        onBackClick = {},
        onAggregateReportClick = {},
        onLastTransactionClick = {},
        onLastTenSuccessfulClick = {},
        onTransactionDetailsClick = {},
        onUnsettledTransactionsClick = {},
        onReprintLastReceiptClick = {},
        onReprintSpecificReceiptClick = {},
        summary = ReportSummary(
        totalAmountToday = "1233",
        successfulTransactionsCount = "55",
    ))
}
