package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.common.R
import com.danesh.common.RowReceipt

fun TransactionResultDetail.isTransferTransaction(): Boolean =
    transactionType == TransactionType.CARD_TO_CARD ||
        transactionType == TransactionType.CARD_TO_WALLET ||
        transactionType == TransactionType.WALLET_TO_WALLET

fun formatTransferCardNumber(value: String): String =
    value.filter { it.isDigit() }.chunked(4).joinToString(" ")

fun TransactionResultDetail.transferDestinationValue(): String = when {
    transactionType == TransactionType.WALLET_TO_WALLET -> formatTransferCardNumber(destinationPan)
    walletCode.isNotBlank() -> walletCode
    destinationPan.isNotBlank() -> formatTransferCardNumber(destinationPan)
    else -> ""
}

@Composable
fun ElectronicTransferReceiptDetails(result: TransactionResultDetail) {
    val sourcePan = result.receiptPan()
    if (sourcePan.isNotBlank()) {
        val sourceLabel = when (result.transactionType) {
            TransactionType.WALLET_TO_WALLET -> stringResource(R.string.receipt_source_wallet_label)
            else -> stringResource(R.string.receipt_source_card_label)
        }
        ElectronicReceiptDetailRow(
            label = sourceLabel,
            value = sourcePan.maskPanForReceipt(),
            icon = R.drawable.ic_pan,
        )
    }

    val destinationValue = result.transferDestinationValue()
    if (destinationValue.isNotBlank()) {
        val destinationLabel = when (result.transactionType) {
            TransactionType.WALLET_TO_WALLET -> stringResource(R.string.receipt_destination_wallet_number_label)
            TransactionType.CARD_TO_WALLET -> stringResource(R.string.receipt_destination_wallet_label)
            else -> stringResource(R.string.receipt_destination_card_label)
        }
        ElectronicReceiptDetailRow(
            label = destinationLabel,
            value = destinationValue,
            icon = R.drawable.ic_pan,
        )
    }

    if (result.holderName.isNotBlank()) {
        ElectronicReceiptDetailRow(
            label = stringResource(R.string.receipt_recipient_name_label),
            value = result.holderName,
            icon = R.drawable.ic_terminal_merchant,
        )
    }
}

@Composable
fun AddTransferReceiptDetails(
    result: TransactionResultDetail,
    modifier: Modifier,
    textColor: Color,
    isPaperReceipt: Boolean,
) {
    val sourcePan = result.receiptPan()
    if (sourcePan.isNotBlank()) {
        val sourceLabel = when (result.transactionType) {
            TransactionType.WALLET_TO_WALLET -> stringResource(R.string.receipt_source_wallet_label)
            else -> stringResource(R.string.receipt_source_card_label)
        }
        RowReceipt(
            modifier = modifier,
            first = sourceLabel,
            second = sourcePan.maskPanForReceipt(),
            textColor = textColor,
            isPaperReceipt = isPaperReceipt,
        )
    }

    val destinationValue = result.transferDestinationValue()
    if (destinationValue.isNotBlank()) {
        val destinationLabel = when (result.transactionType) {
            TransactionType.WALLET_TO_WALLET -> stringResource(R.string.receipt_destination_wallet_number_label)
            TransactionType.CARD_TO_WALLET -> stringResource(R.string.receipt_destination_wallet_label)
            else -> stringResource(R.string.receipt_destination_card_label)
        }
        RowReceipt(
            modifier = modifier,
            first = destinationLabel,
            second = destinationValue,
            textColor = textColor,
            isPaperReceipt = isPaperReceipt,
        )
    }

    if (result.holderName.isNotBlank()) {
        RowReceipt(
            modifier = modifier,
            first = stringResource(R.string.receipt_recipient_name_label),
            second = result.holderName,
            textColor = textColor,
            isPaperReceipt = isPaperReceipt,
        )
    }
}
