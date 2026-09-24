package com.danesh.common.pin

import androidx.annotation.StringRes
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

fun isCommunicationTransactionFailure(response: String): Boolean =
    communicationFailureCode(response) != null

/** کد خطای حمل‌ونقل (نرمال‌شده) یا `null` اگر خطا از نوع ارتباطی نباشد. */
fun communicationFailureCode(response: String): String? {
    val result = parseTransactionResultDetail(response) ?: return null
    if (!TransactionTransportCodes.isTransportCode(result.responseCode)) return null
    return TransactionTransportCodes.normalizeCode(result.responseCode)
}

@StringRes
private fun communicationErrorMessageRes(code: String?): Int = when (code) {
    TransactionTransportCodes.CONNECT_FAILED -> R.string.transaction_communication_error_connect
    TransactionTransportCodes.SEND_FAILED -> R.string.transaction_communication_error_send
    TransactionTransportCodes.RECEIVE_FAILED -> R.string.transaction_communication_error_receive
    TransactionTransportCodes.QUEUE_BLOCKED -> R.string.transaction_communication_error_queue
    else -> R.string.transaction_communication_error_message
}

@Composable
fun TransactionCommunicationErrorDialog(
    onConfirm: () -> Unit,
    errorCode: String? = null,
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
                text = stringResource(communicationErrorMessageRes(errorCode)),
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
