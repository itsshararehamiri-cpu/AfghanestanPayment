package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.settings.util.SettingsTextInputFilters
import com.danesh.ui.textinput.NumericInputContainer
import com.danesh.ui.textinput.NumericOrPlainInputContainer
import com.danesh.ui.textinput.numericInputTextStyle
import com.danesh.ui.textinput.textFieldStyleForKeyboard
import com.danesh.ui.theme.appTextStyle

private val fieldShape = RoundedCornerShape(12.dp)

@Composable
fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    inputFilter: SettingsTextInputFilter = SettingsTextInputFilter.None,
    enabled: Boolean = true,
) {
    val hasError = errorMessage != null
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = if (hasError) Color(0xFFFF5252) else SettingsColors.OptionBorder,
        unfocusedBorderColor = if (hasError) Color(0xFFFF5252) else Color(0xFF144B5B),
        errorBorderColor = Color(0xFFFF5252),
        focusedTextColor = SettingsColors.TextPrimary,
        unfocusedTextColor = SettingsColors.TextPrimary,
        cursorColor = SettingsColors.Accent,
        focusedContainerColor = SettingsColors.OptionBackground,
        unfocusedContainerColor = SettingsColors.OptionBackground,
        focusedPlaceholderColor = Color(0xFFB0BEC5),
        unfocusedPlaceholderColor = Color(0xFFB0BEC5),
    )
    val usesStructuredInput = inputFilter != SettingsTextInputFilter.None
    val effectiveKeyboardType = when (inputFilter) {
        SettingsTextInputFilter.IpAddress -> KeyboardType.Decimal
        SettingsTextInputFilter.DigitsOnly,
        SettingsTextInputFilter.PositiveInteger,
        -> KeyboardType.Number
        SettingsTextInputFilter.None -> keyboardType
    }
    val textStyle = if (usesStructuredInput) {
        numericInputTextStyle()
    } else {
        textFieldStyleForKeyboard(keyboardType)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        label?.let { labelText ->
            Text(
                text = labelText,
                color = SettingsColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val fieldContent: @Composable () -> Unit = {
            if (usesStructuredInput) {
                OutlinedTextField(
                    value = TextFieldValue(
                        text = value,
                        selection = TextRange(value.length),
                    ),
                    onValueChange = { fieldValue ->
                        val filtered = SettingsTextInputFilters.apply(inputFilter, fieldValue.text)
                        onValueChange(filtered)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    isError = hasError,
                    placeholder = {
                        Text(
                            text = placeholder,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = appTextStyle(base = MaterialTheme.typography.bodySmall),
                        )
                    },
                    supportingText = supportingText(errorMessage),
                    shape = fieldShape,
                    colors = fieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = effectiveKeyboardType),
                    textStyle = textStyle,
                )
            } else {
                OutlinedTextField(
                    value = TextFieldValue(
                        text = value,
                        selection = TextRange(value.length),
                    ),
                    onValueChange = { fieldValue ->
                        onValueChange(fieldValue.text)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    isError = hasError,
                    placeholder = {
                        Text(
                            text = placeholder,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            style = appTextStyle(base = MaterialTheme.typography.bodySmall),
                        )
                    },
                    supportingText = supportingText(errorMessage),
                    shape = fieldShape,
                    colors = fieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = effectiveKeyboardType),
                    textStyle = textStyle,
                )
            }
        }

        if (usesStructuredInput) {
            NumericInputContainer(content = fieldContent)
        } else {
            NumericOrPlainInputContainer(keyboardType = effectiveKeyboardType, content = fieldContent)
        }
    }
}

@Composable
private fun supportingText(errorMessage: String?): @Composable (() -> Unit)? =
    errorMessage?.let { message ->
        {
            Text(
                text = message,
                color = Color(0xFFFF5252),
                textAlign = TextAlign.End,
                style = appTextStyle(
                    base = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                ),
            )
        }
    }
