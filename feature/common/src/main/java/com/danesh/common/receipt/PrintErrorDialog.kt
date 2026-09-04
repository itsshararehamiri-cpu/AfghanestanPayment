package com.danesh.common.receipt

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

/**
 * نمایش خطای پرینت: عنوان «خطا در پرینت» + پیام واقعی دستگاه/SDK.
 */
@Composable
fun PrintErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    if (message.isBlank()) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.print_error_title),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.print_error_confirm),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
    )
}
