package com.danesh.report.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.danesh.ui.theme.appFontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.report.ui.theme.ReportColors
import com.danesh.ui.textinput.NumericOrPlainInputContainer
import com.danesh.ui.textinput.textFieldStyleForKeyboard

private val fieldShape = RoundedCornerShape(12.dp)

private val fieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF00E5FF),
        unfocusedBorderColor = ReportColors.FieldBorder,
        focusedTextColor = ReportColors.TextPrimary,
        unfocusedTextColor = ReportColors.TextPrimary,
        disabledTextColor = ReportColors.TextPrimary,
        disabledBorderColor = ReportColors.FieldBorder,
        cursorColor = ReportColors.Accent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = ReportColors.FieldBackground,
        disabledContainerColor = ReportColors.FieldBackground,
        focusedPlaceholderColor = ReportColors.TextSecondary,
        unfocusedPlaceholderColor = ReportColors.TextSecondary,
        disabledPlaceholderColor = ReportColors.TextSecondary,
    )

@Composable
fun ReportFilterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    @DrawableRes iconRes: Int? = null,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    NumericOrPlainInputContainer(keyboardType = keyboardType) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = textFieldStyleForKeyboard(keyboardType, fontSizeSp = 14),
                )
            },
            trailingIcon = {
                if (iconRes != null)
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp),
                    )
            },
            shape = fieldShape,
            colors = fieldColors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = textFieldStyleForKeyboard(keyboardType),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFilterPickerField(
    value: String,
    placeholder: String,
    @DrawableRes iconRes: Int?=null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            trailingIcon = {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
            shape = fieldShape,
            colors = fieldColors,
            enabled = false,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = appFontFamily(),
                textAlign = TextAlign.End,
                fontSize = 16.sp,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFilterDropdownField(
    value: String,
    placeholder: String,
    @DrawableRes iconRes: Int,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
            },
            shape = fieldShape,
            colors = fieldColors,
            readOnly = true,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = appFontFamily(),
                textAlign = TextAlign.End,
                fontSize = 16.sp,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            content()
        }
    }
}
