package com.danesh.report.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.api.TransactionResultDetail
import com.danesh.common.currency.currencyLabel
import com.danesh.common.locale.ReceiptDateTimeContexts
import com.danesh.common.locale.titleRes
import com.danesh.common.receipt.voucherChargeMethodLabel
import com.danesh.report.R
import com.danesh.report.model.NumberedTransaction
import com.danesh.report.ui.theme.ReportColors

private val cardShape = RoundedCornerShape(16.dp)

@Composable
fun LastTenSuccessfulTransactionItem(
    item: NumberedTransaction,
    modifier: Modifier = Modifier,
) {
    val transaction = item.transaction
    val locale = ReceiptDateTimeContexts.current().locale
    val operatorName = resolveOperatorName(transaction)
    val display = LastTenTurnoverDisplayFormatter.format(
        transaction = transaction,
        typeTitle = stringResource(transaction.transactionType.titleRes()),
        topUpTypeTitle = stringResource(com.danesh.common.R.string.tx_type_topup),
        cardPrefix = stringResource(R.string.report_turnover_card_prefix),
        billIdLabel = stringResource(com.danesh.common.R.string.bill_id_label),
        payIdLabel = stringResource(com.danesh.common.R.string.payment_id_label),
        mobileLabel = stringResource(R.string.report_turnover_mobile_label),
        destinationCardLabel = stringResource(com.danesh.common.R.string.receipt_destination_card_label),
        walletLabel = stringResource(R.string.report_turnover_wallet_label),
        voucherSerialLabel = stringResource(R.string.report_turnover_voucher_serial_label),
        currencyLabel = currencyLabel(),
        operatorName = operatorName,
        locale = locale,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0xFF0C2C36).copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0XFF173A46),
                        Color(0XFF14BD46).copy(alpha = 0.5f),
                        Color(0XFF35B7E4).copy(alpha = 0.5f),
                    ),
                ),
                shape = cardShape,
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        LastTenTurnoverSequenceBadge(sequenceNumber = item.sequenceNumber)

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = display.dateTimeLine,
                color = Color(0XFF00FFD4),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = display.typeLine,
                color = ReportColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
            display.detailLines.forEach { line ->
                LastTenTurnoverDetailLineText(
                    line = line,
                    color = ReportColors.TextPrimary.copy(alpha = 0.92f),
                )
            }
            Text(
                text = display.amountLine,
                color = Color(0XFF00FFD4),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun LastTenTurnoverDetailLineText(
    line: LastTenTurnoverDetailLine,
    color: Color,
) {
    val textStyle = MaterialTheme.typography.bodySmall
    if (line.valueLtr) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (line.label.isNotBlank()) {
                Text(
                    text = line.label,
                    color = color,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle,
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = line.value,
                    color = color,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle,
                )
            }
        }
    } else {
        Text(
            text = line.displayText(),
            color = color,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = textStyle,
        )
    }
}

@Composable
private fun resolveOperatorName(transaction: TransactionResultDetail): String {
    val productCode = transaction.productCode.trim()
    if (productCode.isNotEmpty()) {
        return voucherChargeMethodLabel(productCode).trim()
    }
    return transaction.issuerName.trim()
}

@Composable
private fun LastTenTurnoverSequenceBadge(sequenceNumber: Int) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFF0A1F28))
            .border(1.dp, ReportColors.FieldBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = sequenceNumber.toString(),
            color = Color(0XFF00FFD4),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
