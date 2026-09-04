package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danesh.ui.button.GradientActionButton

@Composable
fun ConnectionValueEditSheetContent(
    title: String,
    value: String,
    placeholder: String,
    confirmButtonText: String,
    allowDot: Boolean,
    errorMessage: String?,
    onValueChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
    ) {
        SettingsSheetDragHandle()

        Spacer(modifier = Modifier.height(12.dp))

        SettingsSheetHeader(
            title = title,
            onCloseClick = onCloseClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        ConnectionValueDisplayField(
            value = value,
            placeholder = placeholder,
            errorMessage = errorMessage,
        )

        Spacer(modifier = Modifier.height(20.dp))

        ConnectionValueKeypad(
            allowDot = allowDot,
            onDigitClick = { digit ->
                appendCharacter(
                    current = value,
                    char = digit,
                    allowDot = allowDot,
                )?.let(onValueChange)
            },
            onDotClick = {
                appendCharacter(
                    current = value,
                    char = '.',
                    allowDot = true,
                )?.let(onValueChange)
            },
            onBackspaceClick = {
                if (value.isNotEmpty()) {
                    onValueChange(value.dropLast(1))
                }
            },
            onClearClick = { onValueChange("") },
        )

        Spacer(modifier = Modifier.height(24.dp))

        GradientActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            text = confirmButtonText,
            onClick = onConfirmClick,
        )
    }
}

private fun appendCharacter(
    current: String,
    char: Char,
    allowDot: Boolean,
): String? {
    if (char == '.' && !allowDot) return null
    if (char != '.' && !char.isDigit()) return null

    val next = current + char
    if (allowDot) {
        if (next.length > 15) return null
        if (next.count { it == '.' } > 3) return null
        if (char == '.' && (current.isEmpty() || current.endsWith('.'))) return null
    } else if (next.length > 5) {
        return null
    }

    return next
}
