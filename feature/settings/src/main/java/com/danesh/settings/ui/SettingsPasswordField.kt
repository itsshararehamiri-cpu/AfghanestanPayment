package com.danesh.settings.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.ui.textinput.NumericInputContainer
import com.danesh.ui.textinput.numericInputTextStyle

const val MERCHANT_PASSWORD_MAX_LENGTH = 4

private val fieldShape = RoundedCornerShape(12.dp)

@Composable
fun SettingsPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
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

    NumericInputContainer {
        OutlinedTextField(
            value = TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            ),
            onValueChange = { fieldValue ->
                val filtered = fieldValue.text
                    .filter { it.isDigit() }
                    .take(MERCHANT_PASSWORD_MAX_LENGTH)
                onValueChange(filtered)
            },
            modifier = modifier.fillMaxWidth(),
            isError = hasError,
            placeholder = {
                Text(
                    text = placeholder,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            supportingText = errorMessage?.let { message ->
                {
                    Text(
                        text = message,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (isVisible) {
                            Icons.Outlined.Visibility
                        } else {
                            Icons.Outlined.VisibilityOff
                        },
                        contentDescription = null,
                        tint = SettingsColors.TextPrimary,
                    )
                }
            },
            visualTransformation = if (isVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            shape = fieldShape,
            colors = fieldColors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = numericInputTextStyle(),
        )
    }
}
