package com.danesh.common.pin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.parseTransactionResultDetail
import com.danesh.common.R

fun isCommunicationTransactionFailure(response: String): Boolean {
    val result = parseTransactionResultDetail(response) ?: return false
    return TransactionTransportCodes.isTransportCode(result.responseCode)
}

@Composable
fun TransactionCommunicationErrorDialog(
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        icon = {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = Color(0xFFFF8A80),
            )
        },
        title = {
            Text(
                text = stringResource(R.string.transaction_communication_error_title),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.transaction_communication_error_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.transaction_communication_error_confirm),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
    )
}
