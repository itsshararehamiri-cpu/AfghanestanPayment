package com.danesh.settings.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.danesh.settings.R
import com.danesh.settings.domain.StartupOperation
import com.danesh.settings.domain.StartupStepResult
import com.danesh.settings.ui.theme.SettingsColors

/** سداد: دیالوگ «در حال پیکربندی/شروع به کار» و دیالوگ نتیجه. */
@Composable
fun StartupOperationDialogs(
    inProgress: StartupOperation?,
    result: StartupStepResult?,
    onDismissResult: () -> Unit,
) {
    if (inProgress != null) {
        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = SettingsColors.Accent)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(
                            when (inProgress) {
                                StartupOperation.INIT -> R.string.settings_sadad_init_in_progress
                                StartupOperation.LOGON -> R.string.settings_sadad_logon_in_progress
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            confirmButton = {},
        )
    }
    // پیام «شروع به کار پایانه با موفقیت انجام شد» نمایش داده نمی‌شود؛ فقط خطای شروع به کار.
    val isSilentLogonSuccess =
        result != null && result.operation == StartupOperation.LOGON && result.isSuccess
    if (isSilentLogonSuccess) {
        LaunchedEffect(result) { onDismissResult() }
    }
    if (result != null && inProgress == null && !isSilentLogonSuccess) {
        AlertDialog(
            onDismissRequest = onDismissResult,
            title = {
                Text(
                    text = stringResource(
                        when (result.operation) {
                            StartupOperation.INIT -> R.string.settings_sadad_init
                            StartupOperation.LOGON -> R.string.settings_sadad_logon
                        },
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(text = result.message, style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(onClick = onDismissResult) {
                    Text(
                        text = stringResource(R.string.settings_sadad_ok),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }
}
