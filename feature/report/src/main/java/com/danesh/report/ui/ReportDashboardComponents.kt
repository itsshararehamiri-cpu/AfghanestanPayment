package com.danesh.report.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.report.R
import com.danesh.report.model.ReportSummary
import com.danesh.report.ui.theme.ReportColors

private val cardShape = RoundedCornerShape(16.dp)
private val menuShape = RoundedCornerShape(12.dp)

@Composable
fun ReportSummaryCard(
    summary: ReportSummary,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            //.background(Color(0XFF0C2C36).copy(0.5f))
            .border(
                1.dp, brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0XFF173A46), Color(0XFF14BDF6).copy(0.5f),

                        Color(0XFF35B7E4).copy(0.5f)

                    ),
                ), cardShape
            ),
    ) {

        Image(
            painter = painterResource(R.drawable.background_summary_report),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(140.dp)
                .alpha(0.35f)
                .padding(end = 8.dp), contentScale = ContentScale.FillBounds
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0XFF0C2C36).copy(alpha = 0.5f)
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            ReportSummaryMetric(
                label = stringResource(R.string.report_successful_count),
                value = summary.successfulTransactionsCount,
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(width = 1.dp, height = 72.dp)
                    .background(ReportColors.Background),
            )

            ReportSummaryMetric(
                label = stringResource(R.string.report_total_today),
                value = summary.totalAmountToday,
                suffix = summary.currency,
                modifier = Modifier.weight(1.2f),
            )
        }

    }
}

@Composable
private fun ReportSummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
) {
    Column(
        modifier = modifier.wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = Color(0XFFFFFFFF),
            fontSize = 13.sp,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = value,
            color = Color(0XFF5FFBF3),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodySmall,
        )
        if (suffix != null) {
            Text(
                text = suffix,
                color = ReportColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun ReportMenuButton(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clip(menuShape)
            .background(Color(0XFF0E2632).copy(0.55f))
            .border(1.dp, ReportColors.Background, menuShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(labelRes),
            color = ReportColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 3,
            softWrap = true,
            overflow = TextOverflow.Visible,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun ReportMenuRow(
    onAggregateReportClick: () -> Unit,
    onTransactionDetailsClick: () -> Unit,
    onLastTransactionClick: () -> Unit,
    onLastTenSuccessfulClick: () -> Unit,
    onUnsettledTransactionsClick: () -> Unit,
    onReprintLastReceiptClick: () -> Unit,
    onReprintSpecificReceiptClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
                    ReportMenuButton(
            iconRes = R.drawable.details_transaction,
            labelRes = R.string.report_transaction_details,
            onClick = onTransactionDetailsClick,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
//            ReportMenuButton(
//                iconRes = R.drawable.last_transaction,
//                labelRes = R.string.report_last_transaction,
//                onClick = onLastTransactionClick,
//                modifier = Modifier.weight(1f),
//            )
            ReportMenuButton(
                iconRes = R.drawable.last_transaction_t,
//                labelRes = R.string.report_transaction_details,
//                onClick = onTransactionDetailsClick,
                labelRes = R.string.report_reprint_last_receipt,
                onClick = onReprintLastReceiptClick,
                modifier = Modifier.weight(1f),
            )
            ReportMenuButton(
                iconRes = R.drawable.summary_report,
                labelRes = R.string.report_aggregate,
                onClick = onAggregateReportClick,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(Modifier.weight(1f))

            ReportMenuButton(
                iconRes = R.drawable.last_ten_transactions,
                labelRes = R.string.report_last_ten_successful,
                onClick = onLastTenSuccessfulClick,
                modifier = Modifier.weight(1f),
            )

            ReportMenuButton(
                iconRes = R.drawable.ic_pending_transaction,
                labelRes = R.string.report_unsettled,
                onClick = onUnsettledTransactionsClick,
                modifier = Modifier.weight(1f),
            )

            ReportMenuButton(
                iconRes = R.drawable.ic_tracking,
                labelRes = R.string.report_reprint_specific_receipt,
                onClick = onReprintSpecificReceiptClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
