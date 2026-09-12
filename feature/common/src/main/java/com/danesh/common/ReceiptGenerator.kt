package com.danesh.common
import com.danesh.common.receipt.maskPanForReceipt
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.danesh.common.locale.ReceiptDateTimeContexts
import com.danesh.common.locale.TransactionDateTimeFormatter
import com.danesh.common.locale.displayMerchantName
import com.danesh.common.receipt.formatStanRrnDisplay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.Dimensions.PADDING_SIDE_ROW_RECEIPT
import com.danesh.common.Dimensions.PSP_LOGO_hEIGHT_RECEPINT
import com.danesh.common.receipt.ReceiptType
import com.danesh.common.receipt.truncateMerchantNameForReceipt
import com.danesh.common.receipt.formatAmount
import com.danesh.common.receipt.receiptAmountText
import com.danesh.common.receipt.voucherChargeMethodLabel
import com.danesh.common.currency.amountWithCurrency
import com.danesh.ui.theme.withAppFont

@Composable
fun RowReceipt(
    modifier: Modifier = Modifier,
    first: String,
    second: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = first,
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = 0.dp )
                .layoutId("first"),
            color = textColor,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = getFontSize(isPaperReceipt, context),
                    fontWeight = getFontWeight(isPaperReceipt, context)
                ).withAppFont(),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = second,
            modifier = Modifier
                .wrapContentWidth()
                .padding(start =  0.dp )
                .layoutId("second"),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = getFontSize(isPaperReceipt, context),
                fontWeight = getFontWeight(isPaperReceipt, context)
            ).withAppFont(),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun CenterRowReceipt(
    modifier: Modifier = Modifier,
    first: String,
    second: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
    ) {
        Text(
            text = first,
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = if (isPaperReceipt) 0.dp else 2.dp)
                .layoutId("first"),
            color = textColor,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = getFontSize(isPaperReceipt, context),
                    fontWeight = getFontWeight(isPaperReceipt, context)
                ).withAppFont(),
            textAlign = TextAlign.Start
        )
        Text(
            text = second,
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = if (isPaperReceipt) 0.dp else 2.dp)
                .layoutId("second"),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = getFontSize(isPaperReceipt, context),
                fontWeight = getFontWeight(isPaperReceipt, context)
            ).withAppFont(),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun AddMerchantNamePhone(
    modifier: Modifier = Modifier,
    merchantName: String,
    englishMerchantName: String,
    merchantPhone: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    val displayName = truncateMerchantNameForReceipt(
        name = displayMerchantName(merchantName, englishMerchantName),
        isPaperReceipt = isPaperReceipt,
    )
    RowReceipt(
        modifier = modifier,
        first = displayName,
        second = merchantPhone,
        textColor = textColor,
        isPaperReceipt = isPaperReceipt,
    )
}

@Composable
fun AddMerchantAddress(
    modifier: Modifier = Modifier,
    merchantAddress: String,
    merchantPostalCode: String,
    textColor: Color,
    isPaperReceipt: Boolean = false,
) {
    if (merchantAddress.isNotBlank()) {
        RowReceipt(
            modifier = modifier,
            first = stringResource(R.string.merchant_address),
            second = merchantAddress,
            textColor = textColor,
            isPaperReceipt = isPaperReceipt,
        )
    }
    if (merchantPostalCode.isNotBlank()) {
        RowReceipt(
            modifier = modifier,
            first = stringResource(R.string.merchant_postal_code),
            second = merchantPostalCode,
            textColor = textColor,
            isPaperReceipt = isPaperReceipt,
        )
    }
}

@Composable
fun AddHostReceiptText(
    modifier: Modifier = Modifier,
    text: String?,
    textColor: Color,
    isPaperReceipt: Boolean = false,
    secondText: String? = null,
) {
    val first = text?.trim().orEmpty()
    val second = secondText?.trim().orEmpty()
    if (first.isEmpty() && second.isEmpty()) return
    val context = LocalContext.current
    val style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = getFontSize(isPaperReceipt, context),
        fontWeight = FontWeight.Bold,
    ).withAppFont()
    Column(modifier = modifier.fillMaxWidth()) {
        if (first.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = first,
                    modifier = Modifier.fillMaxWidth(),
                    color = textColor,
                    style = style,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (second.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = second,
                    modifier = Modifier.fillMaxWidth(),
                    color = textColor,
                    style = style,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun AddReceiptType(
    modifier: Modifier = Modifier,
    receiptType: ReceiptType,
    textColor: Color, isPaperReceipt: Boolean = true
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(
                when (receiptType) {
                    ReceiptType.CUSTOMER_RECEIPT -> R.string.customer_receipt
                    ReceiptType.MERCHANT_RECEIPT -> R.string.merchant_receipt
                    ReceiptType.DUPLICATE_RECEIPT -> R.string.reprint_receipt
                    ReceiptType.UNSETTLED_TRANSACTION -> R.string.unsettled_receipt
                    else -> R.string.customer_receipt
                }
            ),
            modifier = Modifier
                .wrapContentWidth()
                .padding(end =  0.dp ),
            color = textColor,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = getFontSize(isPaperReceipt, context),
                    fontWeight = FontWeight.ExtraBold
                ).withAppFont(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddReprintReportTime(
    modifier: Modifier = Modifier,
    reportDateTime: String,
    textColor: Color,
    isPaperReceipt: Boolean = true,
) {
    val context = LocalContext.current
    RowReceipt(
        modifier = modifier,
        first = stringResource(R.string.report_retrieval_time),
        second = reportDateTime,
        textColor = textColor,
        isPaperReceipt = isPaperReceipt,
    )
}

@Composable
fun AddTypeDateTime(
    modifier: Modifier = Modifier,
    type: String,
    date: String,
    time: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    val context = ReceiptDateTimeContexts.current()
    RowReceipt(
        modifier,
        first = type,
        second = TransactionDateTimeFormatter.formatDisplay(
           date,
        time,
            locale = context.locale,
            calendarStyle = context.calendarStyle,
        ),
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}

@Composable
fun AddDateTime(
    modifier: Modifier = Modifier,

    date: String,
    time: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    val context = ReceiptDateTimeContexts.current()
    RowReceipt(
        modifier,
        first = TransactionDateTimeFormatter.formatDate(
            dateYyyyMmDd = date,
            locale = context.locale,
            calendarStyle = context.calendarStyle,
        ),
        second = TransactionDateTimeFormatter.formatTime(time, locale = context.locale),
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}

//@Composable
//fun AddNumberOfAllTransactions(
//    modifier: Modifier = Modifier,
//    number: String,
//    textColor: Color,
//    isPaperReceipt: Boolean = false
//) {
//    RowReceipt(
//        modifier,
//        first = stringResource(R.string.number_of_all_transactions),
//        second = number,
//        textColor = textColor, isPaperReceipt = isPaperReceipt
//    )
//}

//@Composable
//fun AddSumOfAllTransactions(
//    modifier: Modifier = Modifier,
//    sum: String,
//    textColor: Color,
//    isPaperReceipt: Boolean = false
//) {
//    RowReceipt(
//        modifier,
//        first = stringResource(R.string.sum_of_all_transactions),
//        second = stringResource(R.string.amount_with_currency, sum.formatAmount()),
//        textColor = textColor, isPaperReceipt = isPaperReceipt
//    )
//}

@Composable
fun AddBillId(
    modifier: Modifier = Modifier,
    billIdTitle: String,
    billId: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    RowReceipt(
        modifier,
        first = billIdTitle,
        second = billId,
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}

@Composable
fun AddPaymentId(
    modifier: Modifier = Modifier,
    paymentIdTitle: String,
    paymentId: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    RowReceipt(
        modifier,
        first = paymentIdTitle,
        second = paymentId,
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}

@Composable
fun AddMerchantIdTerminalId(
    modifier: Modifier = Modifier,
    merchantId: String,
    terminalId: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    RowReceipt(
        modifier = modifier,
        first = stringResource(R.string.label_terminal_merchant),
        second = "${merchantId}/${terminalId}",
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}

@Composable
fun AddMaskedPanCardIssuer(
    modifier: Modifier = Modifier,
    maskedPan: String,
    cardIssuer: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = cardIssuer.ifEmpty { context.getString(R.string.card_number_title) },
            modifier = Modifier
                .wrapContentWidth()
                .padding(end =  0.dp )
                .layoutId("first"),
            color = textColor,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = getFontSize(isPaperReceipt, context),
                    fontWeight = getFontWeight(isPaperReceipt, context)
                ).withAppFont(),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.weight(1f))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = maskedPan.maskPanForReceipt(),
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(start =  0.dp )
                    .layoutId("second"),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = getFontSize(isPaperReceipt, context),
                    fontWeight = getFontWeight(isPaperReceipt, context)
                ).withAppFont(),
                textAlign = TextAlign.Center
            )
        }
    }
}

//@Composable
//fun AddPurchaseId(
//    modifier: Modifier = Modifier,
//    purchaseId: String,
//    textColor: Color,
//    isPaperReceipt: Boolean = false
//) {
//    val context = LocalContext.current
//
//    Row(
//        modifier = modifier
//            .fillMaxWidth(),
//    ) {
//        Text(
//            text = stringResource(R.string.payment_id),
//            modifier = Modifier
//                .wrapContentWidth()
//                .padding(end = if (isPaperReceipt) 0.dp else PADDING_SIDE_ROW_RECEIPT)
//                .layoutId("first"),
//            color = textColor,
//            style =
//                MaterialTheme.typography.bodyMedium.copy(
//                    fontSize = getFontSize(isPaperReceipt, context = context),
//                    fontWeight = getFontWeight(isPaperReceipt, context = context)
//                ),
//            textAlign = TextAlign.Start
//        )
//        Spacer(modifier = Modifier.weight(1f))
//        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
//            Text(
//                text = purchaseId,
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .padding(start = if (isPaperReceipt) 0.dp else PADDING_SIDE_ROW_RECEIPT)
//                    .layoutId("second"),
//                color = textColor,
//                style = MaterialTheme.typography.bodyMedium.copy(
//                    fontSize = getFontSize(isPaperReceipt, context),
//                    fontWeight = getFontWeight(isPaperReceipt, context)
//                ),
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}

@Composable
fun AddRRNStan(
    modifier: Modifier = Modifier,
    rrn: String?,
    stan: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    val displayValue = formatStanRrnDisplay(stan, rrn)
    if (displayValue.isBlank()) return
    RowReceipt(
        modifier = modifier,
        first = if (rrn.isNullOrBlank()) stringResource(R.string.trace__)
        else stringResource(R.string.trace_rrn),
        second = displayValue,
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}

@Composable
fun AddAmount(
    modifier: Modifier = Modifier,
    amount: String, textColor: Color,
    isPaperReceipt: Boolean = false
) {

    val context = LocalContext.current
    var tempModifier = modifier
        .fillMaxWidth()
    if (isPaperReceipt)
        tempModifier =
            tempModifier
                .border(width = 1.dp, shape = RoundedCornerShape(3.dp), color = textColor)
                .padding(horizontal = 2.dp)
                .padding(top = 1.dp, bottom = 1.dp)
    Row(
        modifier  = modifier
            .fillMaxWidth(),
    ) {


        Text(
            text = stringResource(R.string.amount),
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = if (isPaperReceipt) 0.dp else PADDING_SIDE_ROW_RECEIPT)
                .layoutId("first"),
            color = textColor,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = getFontSize(isPaperReceipt, context),
                    fontWeight = getFontWeight(isPaperReceipt, context)
                ).withAppFont(),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = amountWithCurrency(receiptAmountText(amount)),
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = if (isPaperReceipt) 0.dp else PADDING_SIDE_ROW_RECEIPT)
                .layoutId("second"),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = getFontSize(isPaperReceipt, context),
                fontWeight =
                    if (isPaperReceipt) {

                        FontWeight.ExtraBold

                    } else {
                        FontWeight.Bold
                    }

            ).withAppFont(),
            textAlign = TextAlign.Start
        )

    }
}
//
//@Composable
//fun AddPosCode(
//    modifier: Modifier = Modifier,
//    posCode: String, textColor: Color,
//    isPaperReceipt: Boolean = false
//) {
//    RowReceipt(
//        modifier = modifier,
//        first = stringResource(R.string.pos_code),
//        second = posCode,
//        textColor = textColor, isPaperReceipt = isPaperReceipt
//    )
//}
//
@Composable
fun AddMobile(
    modifier: Modifier = Modifier,
    mobile: String, textColor: Color,
    isPaperReceipt: Boolean = false
) {

    RowReceipt(
        modifier = modifier,
        first = stringResource(R.string.mobile_number),
        second = mobile,
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}

@Composable
fun AddBalance(
    modifier: Modifier = Modifier,
    balance: String, textColor: Color,
    isPaperReceipt: Boolean = false
) {

    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = stringResource(com.danesh.common.R.string.label_available_balance),
            modifier = Modifier
                .wrapContentWidth()
                .padding(end =0.dp )
                .layoutId("first"),
            color = textColor,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = getFontSize(isPaperReceipt, context),
                    fontWeight = getFontWeight(isPaperReceipt, context)
                ).withAppFont(),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = amountWithCurrency(receiptAmountText(balance)),
            modifier = Modifier
                .wrapContentWidth()
                .padding(start =  0.dp )
                .layoutId("second"),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = getFontSize(isPaperReceipt, context),
                fontWeight =

                            FontWeight.Medium



            ),
            textAlign = TextAlign.Start
        )
    }
}
//
@Composable
fun AddFee(
    modifier: Modifier = Modifier,
    fee: String,
    textColor: Color,
    isPaperReceipt: Boolean = false,
    feeLabelRes: Int = R.string.transaction_fee,
) {
    CenterRowReceipt(
        modifier = modifier,
        first = stringResource(feeLabelRes),
        second = amountWithCurrency(receiptAmountText(fee)),
        textColor = textColor,
        isPaperReceipt = isPaperReceipt,
    )
}

@Composable
fun AddBalanceFee(
    modifier: Modifier = Modifier,
    fee: String,
    textColor: Color,
    isPaperReceipt: Boolean = false,
) {
    AddFee(
        modifier = modifier,
        fee = fee,
        textColor = textColor,
        isPaperReceipt = isPaperReceipt,
        feeLabelRes = R.string.balance_transaction_fee,
    )
}


@Composable
fun AddAvailableBalance(
    modifier: Modifier = Modifier,
    balance: String, textColor: Color,
    isPaperReceipt: Boolean = false
) {
 //
}

@Composable
fun AddVoucherSerial(
    modifier: Modifier = Modifier,
    voucherSerial: String?,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    RowReceipt(
        modifier = modifier,
        first = stringResource(R.string.voucher_serial),
        second = voucherSerial ?: "",
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}

@Composable
fun AddVoucherPin(
    modifier: Modifier = Modifier,
    voucherPin: String?,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    RowReceipt(
        modifier = modifier,
        first = stringResource(R.string.voucher_pin), second = voucherPin ?: "",
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}

@Composable
fun AddVoucherChargeMSG(
    modifier: Modifier = Modifier,
    operatorCode: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    val chargeMethod = voucherChargeMethodLabel(operatorCode)
    if (chargeMethod.isBlank()) return
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.charging_method),
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = if (isPaperReceipt) 0.dp else PADDING_SIDE_ROW_RECEIPT)
                .layoutId("first"),
            color = textColor,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = getFontSize(isPaperReceipt, context),
                    fontWeight = getFontWeight(isPaperReceipt, context)
                ).withAppFont(),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = chargeMethod,
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = if (isPaperReceipt) 0.dp else PADDING_SIDE_ROW_RECEIPT)
                .layoutId("second"),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = getFontSize(isPaperReceipt, context),
                fontWeight = getFontWeight(isPaperReceipt, context)
            ).withAppFont(),
            textAlign = TextAlign.Start
        )

    }
}

//
@Composable
fun AddCustomerSignature(
    modifier: Modifier = Modifier,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
//    RowReceipt(
//        modifier = modifier,
//        first = stringResource(R.string.customer_signature),
//        second = "",
//        textColor = textColor, isPaperReceipt = isPaperReceipt
//    )
//
}

@Composable
fun ShowSuccessResult(modifier: Modifier, firstColor: Color) {
    Text(
        modifier = modifier,
        text = stringResource(id = com.danesh.common.R.string.success_result),
        color = firstColor,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold).withAppFont()
    )
}