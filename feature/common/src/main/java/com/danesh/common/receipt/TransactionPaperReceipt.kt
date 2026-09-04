package com.danesh.common.receipt

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danesh.api.TransactionResultDetail
import com.danesh.common.R
import com.danesh.common.getFontSize
import com.danesh.common.getFontSizeUnSuccess
import com.danesh.api.TransactionType
import com.danesh.common.AddAmount
import com.danesh.common.AddAvailableBalance
import com.danesh.common.AddBalance
import com.danesh.common.AddBillId
import com.danesh.common.AddHostReceiptText
import com.danesh.common.AddMaskedPanCardIssuer
import com.danesh.common.AddMerchantIdTerminalId
import com.danesh.common.AddMerchantNamePhone
import com.danesh.common.AddMobile
import com.danesh.common.AddPaymentId
import com.danesh.common.AddRRNStan
import com.danesh.common.AddReprintReportTime
import com.danesh.common.AddReceiptType
import com.danesh.common.AddTypeDateTime
import com.danesh.common.AddVoucherChargeMSG
import com.danesh.common.AddVoucherPin
import com.danesh.common.AddVoucherSerial
import com.danesh.common.HorizontalDivider
import com.danesh.common.ShowSuccessResult
import com.danesh.common.containerReceiptModifier
import com.danesh.common.locale.titleRes
import com.danesh.common.rowReceiptModifier
import com.danesh.common.rowReceiptWithPSPLogoModifier

@Composable
fun TransactionPaperReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType = ReceiptType.DUPLICATE_RECEIPT,
    isPaperReceipt: Boolean = true,
) {
    Log.d("TAG", "TransactionPaperReceipt: hhh$receiptType")
    Log.d("TAG", "TransactionPaperReceipt: hhh$result")

    when (result.transactionType) {
        TransactionType.BALANCE -> BalancePaperReceipt(
            result,
            receiptType,
            isPaperReceipt,
        )
        TransactionType.BILL -> BillPaperReceipt(result, receiptType, isPaperReceipt)
        TransactionType.VOUCHER -> VoucherPaperReceipt(result, receiptType, isPaperReceipt)
        TransactionType.TOPUP -> TopUpPaperReceipt(result, receiptType, isPaperReceipt)
        TransactionType.CASH_DEPOSIT -> CashDepositPaperReceipt(result, receiptType, isPaperReceipt)
        TransactionType.CASH_OUT -> CashOutPaperReceipt(result, receiptType, isPaperReceipt)
        TransactionType.CARD_TO_CARD, TransactionType.CARD_TO_WALLET, TransactionType.WALLET_TO_WALLET ->
            TransferPaperReceipt(result, receiptType, isPaperReceipt)
        else -> StandardPaperReceipt(result, receiptType, isPaperReceipt)
    }
}

@Composable
private fun StandardPaperReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType,
    isPaperReceipt: Boolean,
) {
    val context = LocalContext.current
    val firstColor = if (isPaperReceipt) Color.Black else MaterialTheme.colorScheme.onSurface
    val modifierRowReceipt = Modifier.rowReceiptModifier(isPaperReceipt)
    val maskedPan = result.receiptPan()
    val transactionTitle = stringResource(result.transactionType.titleRes())
    Log.d("TAG", "StandardPaperReceipt: ddddddddddgggg${result.isSuccess}")
    Column(modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)) {
        PaperReceiptHeader(result, receiptType, isPaperReceipt, firstColor, modifierRowReceipt)
        if (result.payId.isNotBlank()) {
            AddPaymentId(
                modifier = modifierRowReceipt,
                paymentIdTitle = stringResource(com.danesh.common.R.string.deposit_id_label),
                paymentId = result.payId,
                textColor = firstColor,
                isPaperReceipt = isPaperReceipt,
            )
        }
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = transactionTitle,
            date = result.date,
            time = result.time,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        HorizontalDivider(isPaperReceipt = isPaperReceipt)
        PaperReceiptCommonBody(result, isPaperReceipt, firstColor, modifierRowReceipt, maskedPan)
        if (result.amount.isNotBlank()) {
            AddAmount(
                modifier = modifierRowReceipt,
                result.amount,
                isPaperReceipt = isPaperReceipt,
                textColor = firstColor,
            )
        }
        PaperReceiptFooter(result, isPaperReceipt, firstColor)
    }
}

@Composable
private fun BalancePaperReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType,
    isPaperReceipt: Boolean,
) {
    val context = LocalContext.current
    val firstColor = if (isPaperReceipt) Color.Black else MaterialTheme.colorScheme.onSurface
    val modifierRowReceipt = Modifier.rowReceiptModifier(isPaperReceipt)
    val maskedPan = result.receiptPan()

    Column(modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)) {
        PaperReceiptHeader(result, receiptType, isPaperReceipt, firstColor, modifierRowReceipt)
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(result.transactionType.titleRes()),
            date = result.date,
            time = result.time,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        HorizontalDivider(isPaperReceipt = isPaperReceipt)
        PaperReceiptCommonBody(result, isPaperReceipt, firstColor, modifierRowReceipt, maskedPan)
        val actual = result.actualBalance?.takeIf { it.isNotBlank() }
        val available = result.availableBalance?.takeIf { it.isNotBlank() }
        if (actual != null) {
            AddBalance(
                modifier = modifierRowReceipt,
                actual,
                textColor = firstColor,
                isPaperReceipt = isPaperReceipt,
            )
        }
        if (available != null && available != actual) {
            AddAvailableBalance(
                modifier = modifierRowReceipt,
                available,
                isPaperReceipt = isPaperReceipt,
                textColor = firstColor,
            )
        } else if (actual == null && available != null) {
            AddAvailableBalance(
                modifier = modifierRowReceipt,
                available,
                isPaperReceipt = isPaperReceipt,
                textColor = firstColor,
            )
        }
        PaperReceiptFooter(result, isPaperReceipt, firstColor)
    }
}

@Composable
private fun BillPaperReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType,
    isPaperReceipt: Boolean,
) {
    val context = LocalContext.current
    val firstColor = if (isPaperReceipt) Color.Black else MaterialTheme.colorScheme.onSurface
    val modifierRowReceipt = Modifier.rowReceiptModifier(isPaperReceipt)
    val maskedPan = result.receiptPan()

    Column(modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)) {
        PaperReceiptHeader(result, receiptType, isPaperReceipt, firstColor, modifierRowReceipt)
        if (result.billId.isNotBlank()) {
            AddBillId(
                modifier = modifierRowReceipt,
                billIdTitle = stringResource(com.danesh.common.R.string.bill_id_label),
                billId = result.billId,
                textColor = firstColor,
                isPaperReceipt = isPaperReceipt,
            )
        }
        if (result.payId.isNotBlank()) {
            AddPaymentId(
                modifier = modifierRowReceipt,
                paymentIdTitle = stringResource(com.danesh.common.R.string.payment_id_label),
                paymentId = result.payId,
                textColor = firstColor,
                isPaperReceipt = isPaperReceipt,
            )
        }
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(result.transactionType.titleRes()),
            date = result.date,
            time = result.time,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        HorizontalDivider(isPaperReceipt = isPaperReceipt)
        PaperReceiptCommonBody(result, isPaperReceipt, firstColor, modifierRowReceipt, maskedPan)
        if (result.amount.isNotBlank()) {
            AddAmount(
                modifier = modifierRowReceipt,
                result.amount,
                isPaperReceipt = isPaperReceipt,
                textColor = firstColor,
            )
        }
        PaperReceiptFooter(result, isPaperReceipt, firstColor)
    }
}

@Composable
private fun VoucherPaperReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType,
    isPaperReceipt: Boolean,
) {
    val context = LocalContext.current
    val firstColor = if (isPaperReceipt) Color.Black else MaterialTheme.colorScheme.onSurface
    val modifierRowReceipt = Modifier.rowReceiptModifier(isPaperReceipt)
    val maskedPan = result.receiptPan()

    Column(modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)) {
        PaperReceiptHeader(result, receiptType, isPaperReceipt, firstColor, modifierRowReceipt)
        if (receiptType.showsVoucherChargeDetails()) {
            if (result.voucherSerial.isNotBlank()) {
                AddVoucherSerial(
                    modifier = modifierRowReceipt,
                    voucherSerial = result.voucherSerial,
                    textColor = firstColor,
                    isPaperReceipt = isPaperReceipt,
                )
            }
            if (result.voucherPin.isNotBlank()) {
                AddVoucherPin(
                    modifier = modifierRowReceipt,
                    voucherPin = result.voucherPin,
                    textColor = firstColor,
                    isPaperReceipt = isPaperReceipt,
                )
            }
            if (result.productCode.isNotBlank()) {
                AddVoucherChargeMSG(
                    modifier = modifierRowReceipt,
                    operatorCode = result.productCode,
                    textColor = firstColor,
                    isPaperReceipt = isPaperReceipt,
                )
            }
        }
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(result.transactionType.titleRes()),
            date = result.date,
            time = result.time,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        HorizontalDivider(isPaperReceipt = isPaperReceipt)
        PaperReceiptCommonBody(result, isPaperReceipt, firstColor, modifierRowReceipt, maskedPan)
        if (result.amount.isNotBlank()) {
            AddAmount(
                modifier = modifierRowReceipt,
                result.amount,
                isPaperReceipt = isPaperReceipt,
                textColor = firstColor,
            )
        }
        PaperReceiptFooter(result, isPaperReceipt, firstColor)
    }
}

@Composable
private fun TopUpPaperReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType,
    isPaperReceipt: Boolean,
) {
    val context = LocalContext.current
    val firstColor = if (isPaperReceipt) Color.Black else MaterialTheme.colorScheme.onSurface
    val modifierRowReceipt = Modifier.rowReceiptModifier(isPaperReceipt)
    val maskedPan = result.receiptPan()

    Column(modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)) {
        PaperReceiptHeader(result, receiptType, isPaperReceipt, firstColor, modifierRowReceipt)
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(result.transactionType.titleRes()),
            date = result.date,
            time = result.time,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        HorizontalDivider(isPaperReceipt = isPaperReceipt)
        PaperReceiptCommonBody(result, isPaperReceipt, firstColor, modifierRowReceipt, maskedPan)
        if (result.mobileNumber.isNotBlank()) {
            AddMobile(
                modifier = modifierRowReceipt,
                mobile = result.mobileNumber,
                textColor = firstColor,
                isPaperReceipt = isPaperReceipt,
            )
        }
        if (result.amount.isNotBlank()) {
            AddAmount(
                modifier = modifierRowReceipt,
                result.amount,
                isPaperReceipt = isPaperReceipt,
                textColor = firstColor,
            )
        }
        PaperReceiptFooter(result, isPaperReceipt, firstColor)
    }
}

@Composable
private fun CashDepositPaperReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType,
    isPaperReceipt: Boolean,
) {
    val context = LocalContext.current
    val firstColor = if (isPaperReceipt) Color.Black else MaterialTheme.colorScheme.onSurface
    val modifierRowReceipt = Modifier.rowReceiptModifier(isPaperReceipt)
    val maskedPan = result.receiptPan()

    Column(modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)) {
        AddMerchantNamePhone(
            modifier = Modifier.rowReceiptWithPSPLogoModifier(isPaperReceipt),
            merchantName = result.merchantName,
            merchantPhone = result.merchantPhone,
            englishMerchantName = result.englishMerchantName,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        if (isPaperReceipt) {
            AddReceiptType(
                modifier = modifierRowReceipt,
                receiptType = receiptType,
                textColor = firstColor,
            )
        }
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(result.transactionType.titleRes()),
            date = result.date,
            time = result.time,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        HorizontalDivider(isPaperReceipt = isPaperReceipt)
        PaperReceiptCommonBody(result, isPaperReceipt, firstColor, modifierRowReceipt, maskedPan)
        result.availableBalance?.takeIf { it.isNotBlank() }?.let { balance ->
            AddAmount(
                modifier = modifierRowReceipt,
                balance,
                isPaperReceipt = isPaperReceipt,
                textColor = firstColor,
            )
        }
        PaperReceiptFooter(result, isPaperReceipt, firstColor)
    }
}

@Composable
private fun TransferPaperReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType,
    isPaperReceipt: Boolean,
) {
    val context = LocalContext.current
    val firstColor = if (isPaperReceipt) Color.Black else MaterialTheme.colorScheme.onSurface
    val modifierRowReceipt = Modifier.rowReceiptModifier(isPaperReceipt)

    Column(modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)) {
        PaperReceiptHeader(result, receiptType, isPaperReceipt, firstColor, modifierRowReceipt)
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(result.transactionType.titleRes()),
            date = result.date,
            time = result.time,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        HorizontalDivider(isPaperReceipt = isPaperReceipt)
        AddMerchantIdTerminalId(
            modifier = modifierRowReceipt,
            merchantId = result.merchantId,
            terminalId = result.terminalId,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        AddTransferReceiptDetails(
            result = result,
            modifier = modifierRowReceipt,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        AddRRNStan(
            modifier = modifierRowReceipt,
            rrn = result.rrn,
            stan = result.trace,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        if (result.amount.isNotBlank()) {
            AddAmount(
                modifier = modifierRowReceipt,
                result.amount,
                isPaperReceipt = isPaperReceipt,
                textColor = firstColor,
            )
        }
        PaperReceiptFooter(result, isPaperReceipt, firstColor)
    }
}

@Composable
private fun CashOutPaperReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType,
    isPaperReceipt: Boolean,
) = CashDepositPaperReceipt(result, receiptType, isPaperReceipt)

@Composable
private fun PaperReceiptHeader(
    result: TransactionResultDetail,
    receiptType: ReceiptType,
    isPaperReceipt: Boolean,
    firstColor: Color,
    modifierRowReceipt: Modifier,
) {
    AddHostReceiptText(
        modifier = modifierRowReceipt,
        text = result.hostReceiptText,
        secondText = result.hostReceiptTextSecond,
        textColor = firstColor,
        isPaperReceipt = isPaperReceipt,
    )
    if (isPaperReceipt) {
        AddReceiptType(
            modifier = modifierRowReceipt,
            receiptType = receiptType,
            textColor = firstColor,
        )
    }
    result.reprintReportDateTime?.takeIf { it.isNotBlank() }?.let { reportTime ->
        AddReprintReportTime(
            modifier = modifierRowReceipt,
            reportDateTime = reportTime,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
    }
    AddMerchantNamePhone(
        modifier = Modifier.rowReceiptWithPSPLogoModifier(isPaperReceipt),
        merchantName = result.merchantName,
        merchantPhone = result.merchantPhone,
        englishMerchantName = result.englishMerchantName,
        textColor = firstColor,
        isPaperReceipt = isPaperReceipt,
    )
}

@Composable
private fun PaperReceiptCommonBody(
    result: TransactionResultDetail,
    isPaperReceipt: Boolean,
    firstColor: Color,
    modifierRowReceipt: Modifier,
    maskedPan: String,
) {
    AddMerchantIdTerminalId(
        modifier = modifierRowReceipt,
        merchantId = result.merchantId,
        terminalId = result.terminalId,
        textColor = firstColor,
        isPaperReceipt = isPaperReceipt,
    )
    if (maskedPan.isNotBlank()) {
        AddMaskedPanCardIssuer(
            modifier = modifierRowReceipt,
            maskedPan = maskedPan,
            cardIssuer = result.issuerName,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
    }
    AddRRNStan(
        modifier = modifierRowReceipt,
        rrn = result.rrn,
        stan = result.trace,
        textColor = firstColor,
        isPaperReceipt = isPaperReceipt,
    )
}

@Composable
private fun ColumnScope.PaperReceiptFooter(
    result: TransactionResultDetail,
    isPaperReceipt: Boolean,
    firstColor: Color,
) {
    if (result.isSuccess) {
        ShowSuccessResult(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally),
            firstColor = firstColor,
        )
    } else {
        UnSuccessPaperReceiptFooter(result, isPaperReceipt, firstColor)
    }
    if (isPaperReceipt) {
        AddPSPLog(
            modifier = Modifier.fillMaxWidth(),
            color = firstColor,
            isPaperReceipt = true,
        )
    }
}
