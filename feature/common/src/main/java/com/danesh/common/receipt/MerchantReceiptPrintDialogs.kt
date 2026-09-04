package com.danesh.common.receipt

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun MerchantReceiptOptionalDialog(
    onConfirm: () -> Unit,
    onDecline: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = {
            Text(
                text = stringResource(R.string.merchant_receipt_optional_dialog_title),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.merchant_receipt_optional_dialog_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.merchant_receipt_dialog_confirm),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(
                    text = stringResource(R.string.merchant_receipt_dialog_decline),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
    )
}

@Composable
fun MerchantReceiptMandatoryDialog(
    onContinue: () -> Unit,
) {
    Box(
        modifier = Modifier
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp) {
                    onContinue()
                    true
                } else {
                    false
                }
            },
    ) {
        AlertDialog(
            onDismissRequest = onContinue,
            title = {
                Text(
                    text = stringResource(R.string.merchant_receipt_mandatory_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.merchant_receipt_mandatory_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onContinue) {
                    Text(
                        text = stringResource(R.string.merchant_receipt_dialog_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }
}
