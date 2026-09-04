package com.danesh.common.qr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.danesh.common.currency.currencyLabel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.NfcBottomPanel
import com.danesh.common.R
import com.danesh.common.SwipeUpIndicator
import com.danesh.common.receipt.ElectronicReceiptDetailRow
import com.danesh.common.receipt.PurchaseSolidDivider
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar
import java.util.UUID

data class QrPaymentDetails(
    val paymentProviderName: String,
    val transactionType: String,
    val merchantName: String,
    val payableAmount: String,
)

@Composable
fun QrScanScreen(
    qrContent: String,
    details: QrPaymentDetails,
    onBackClick: () -> Unit = {},
    onNfcPanelClick: () -> Unit = {},
) {
    val screenInstanceId = remember { UUID.randomUUID().toString() }
    val qrBitmap = remember(qrContent, screenInstanceId) {
        generateQrCodeBitmap(
            content = qrContent.ifBlank { screenInstanceId },
                        foregroundColor = Color(0xFF00FFD4),
                    )
}

Box(
    modifier = Modifier
        .fillMaxSize()
        .appScreenBackground(),
) {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(stringResource(R.string.balance_qr_scan_title)) {
                onBackClick()
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                QrCodeWithFrame(
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                    qrBitmap = qrBitmap,
                )

                Text(
                    text = stringResource(R.string.balance_qr_waiting_hint),
                    color = Color(0XFFFFFFFF),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp),
                    style = MaterialTheme.typography.bodySmall,
                )

                QrPaymentInfoCard(details = details)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                SwipeUpIndicator()
            }

            NfcBottomPanel(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNfcPanelClick,
            )
        }
    }
}

@Composable
private fun QrCodeWithFrame(
    modifier: Modifier = Modifier,
    qrBitmap: ImageBitmap,
) {
    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        QrCornerBrackets(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFFFFFF),
        )
        Image(
            bitmap = qrBitmap,
            contentDescription = stringResource(R.string.balance_qr_scan_title),
            modifier = Modifier.size(168.dp),
        )
    }
}

@Composable
private fun QrCornerBrackets(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Float = 3f,
    cornerLengthRatio: Float = 0.18f,
) {
    Canvas(modifier = modifier) {
        val length = size.minDimension * cornerLengthRatio
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        fun cornerPath(start: Offset, corner: Offset, end: Offset): Path = Path().apply {
            moveTo(start.x, start.y)
            lineTo(corner.x, corner.y)
            lineTo(end.x, end.y)
        }

        drawPath(
            cornerPath(Offset(0f, length), Offset.Zero, Offset(length, 0f)),
            color = color,
            style = stroke,
        )
        drawPath(
            cornerPath(
                Offset(size.width - length, 0f),
                Offset(size.width, 0f),
                Offset(size.width, length),
            ),
            color = color,
            style = stroke,
        )
        drawPath(
            cornerPath(
                Offset(0f, size.height - length),
                Offset(0f, size.height),
                Offset(length, size.height),
            ),
            color = color,
            style = stroke,
        )
        drawPath(
            cornerPath(
                Offset(size.width - length, size.height),
                Offset(size.width, size.height),
                Offset(size.width, size.height - length),
            ),
            color = color,
            style = stroke,
        )
    }
}

@Composable
private fun QrPaymentInfoCard(details: QrPaymentDetails) {
    val cardShape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(cardShape)
            .background(Color(0XFF012734))
            .border(
                width = 1.dp,
                color = Color(0XFFFF144B5B),
                shape = cardShape,
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        ElectronicReceiptDetailRow(
            label = stringResource(R.string.balance_label_payment_company),
            icon =R.drawable.ic_payment_company,
            labelColor = Color(0xFFFFFFFF),
            verticalPadding = 14.dp,
            valueContent = { PaymentProviderBadge(name = details.paymentProviderName) },
        )
        PurchaseSolidDivider()
        ElectronicReceiptDetailRow(
            label = stringResource(R.string.balance_label_transaction_type),
            icon = com.danesh.ui.R.drawable.ic_transaction_type,
            labelColor =Color(0xFFFFFFFF),
            value = details.transactionType,
            valueColor = Color(0xFFFFFFFF),
            verticalPadding = 14.dp,
        )
        PurchaseSolidDivider()
        ElectronicReceiptDetailRow(
            label = stringResource(R.string.balance_label_merchant_name),
            icon = R.drawable.ic_payment_company,
            labelColor = Color(0xFFFFFFFF),
            value = details.merchantName,
            valueColor = Color(0xFFFFFFFF),
            verticalPadding = 14.dp,
        )
        PurchaseSolidDivider()
        ElectronicReceiptDetailRow(
            label = stringResource(R.string.balance_label_payable_amount),
            icon = R.drawable.ic_payable_amount,
            labelColor = Color(0xFFFFFFFF),
            verticalPadding = 14.dp,
            valueContent = { PayableAmountValue(amount = details.payableAmount) },
        )
    }
}

@Composable
private fun PayableAmountValue(amount: String) {
    val label = currencyLabel()
    val amountValue = amount.removeSuffix(" $label").trim()
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = amountValue,
            color = Color(0XFF5FFBF3),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = label,
            color =Color(0xFFFFFFFF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PaymentProviderBadge(name: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = stringResource(R.string.currency_asp),
            color = Color(0xFFFFFFFF),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = name,
            color = Color(0xFFFFFFFF),
            fontSize = 10.sp,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

//@Preview(showBackground = true, backgroundColor = 0xFF001A1F, widthDp = 360, heightDp = 780)
//@Composable
//private fun QrScanScreenPreview() {
//    QrScanScreen(
//        qrContent = "pay://sample/2200",
//        details = QrPaymentDetails(
//            paymentProviderName = "Payment Provider",
//            transactionType = "خرید",
//            merchantName = "شرکت همراه پی",
//            payableAmount = "2,200 AFN",
//        ),
//    )
//}
