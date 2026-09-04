package com.danesh.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.settings.R
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.ui.textinput.NumericInputContainer
import com.danesh.ui.textinput.numericInputTextStyle
import com.danesh.ui.theme.appTextStyle

private val fieldShape = RoundedCornerShape(12.dp)
private val keypadShape = RoundedCornerShape(14.dp)
private val keypadBorder = Color(0xFF144B5B)
private val keypadBackground = Color(0xFF0C2C36)

private val keypadRows = listOf(
    listOf('3', '2', '1'),
    listOf('6', '5', '4'),
    listOf('9', '8', '7'),
)

@Composable
fun ConnectionValueDisplayField(
    value: String,
    placeholder: String,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    val hasError = errorMessage != null
    Column(modifier = modifier.fillMaxWidth()) {
        NumericInputContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(fieldShape)
                    .background(SettingsColors.OptionBackground)
                    .border(
                        width = 1.dp,
                        color = if (hasError) Color(0xFFFF5252) else Color(0xFF144B5B),
                        shape = fieldShape,
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = value.ifBlank { placeholder },
                    color = if (value.isBlank()) {
                        Color(0xFFB0BEC5)
                    } else {
                        SettingsColors.TextPrimary
                    },
                    textAlign = TextAlign.Start,
                    style = numericInputTextStyle(fontSizeSp = 16),
                )
            }
        }
        errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(top = 6.dp),
                color = Color(0xFFFF5252),
                textAlign = TextAlign.Start,
                style = appTextStyle(
                    base = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                ),
            )
        }
    }
}

@Composable
fun ConnectionValueKeypad(
    allowDot: Boolean,
    onDigitClick: (Char) -> Unit,
    onDotClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        keypadRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { digit ->
                    KeypadDigitButton(
                        label = digit.toPersianDigit(),
                        modifier = Modifier.weight(1f),
                        onClick = { onDigitClick(digit) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KeypadActionButton(
                label = stringResource(R.string.settings_support_keypad_backspace),
                modifier = Modifier.weight(1f),
                onClick = onBackspaceClick,
            )
            KeypadDigitButton(
                label = '0'.toPersianDigit(),
                modifier = Modifier.weight(1f),
                onClick = { onDigitClick('0') },
            )
            if (allowDot) {
                KeypadDigitButton(
                    label = ".",
                    modifier = Modifier.weight(1f),
                    onClick = onDotClick,
                )
            } else {
                KeypadActionButton(
                    label = stringResource(R.string.settings_support_keypad_clear),
                    modifier = Modifier.weight(1f),
                    onClick = onClearClick,
                )
            }
        }
    }
}

private fun Char.toPersianDigit(): String = when (this) {
    '0' -> "۰"
    '1' -> "۱"
    '2' -> "۲"
    '3' -> "۳"
    '4' -> "۴"
    '5' -> "۵"
    '6' -> "۶"
    '7' -> "۷"
    '8' -> "۸"
    '9' -> "۹"
    else -> toString()
}

@Composable
private fun KeypadDigitButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(keypadShape)
            .background(keypadBackground.copy(alpha = 0.85f))
            .border(1.dp, keypadBorder, keypadShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            style = appTextStyle(
                base = MaterialTheme.typography.titleMedium,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun KeypadActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(keypadShape)
            .background(keypadBackground.copy(alpha = 0.85f))
            .border(1.dp, keypadBorder, keypadShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            style = appTextStyle(
                base = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
