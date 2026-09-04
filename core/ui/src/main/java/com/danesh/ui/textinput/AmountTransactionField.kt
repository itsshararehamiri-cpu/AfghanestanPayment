package com.danesh.ui.textinput

import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.OutlinedTextFieldDefaults

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.ColorFilter

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.danesh.ui.theme.AppColors
import com.danesh.ui.theme.appTextStyle



@Composable

 fun AmountTransactionField(

    label: String,

    value: String,

    onValueChange: (String) -> Unit,

    placeholder: String,

    iconRes: Int,

    keyboardType: KeyboardType,

    errorMessage: String? = null,

) {

    val hasError = errorMessage != null

    val fieldShape = RoundedCornerShape(12.dp)

    val fieldColors = OutlinedTextFieldDefaults.colors(

        focusedBorderColor = if (hasError) Color(0xFFFF5252) else Color(0xFF00E5FF),

        unfocusedBorderColor = if (hasError) Color(0xFFFF5252) else Color(0xFF144B5B),

        errorBorderColor = Color(0xFFFF5252),

        focusedTextColor = AppColors.TextOnBackground,

        unfocusedTextColor = AppColors.TextOnBackground,

        cursorColor = Color(0XFF5FFBF3),

        focusedContainerColor = Color.Transparent,

        unfocusedContainerColor = Color(0xFF000000).copy(alpha = 0.19f),

        focusedPlaceholderColor = Color(0xFFB0BEC5),

        unfocusedPlaceholderColor = Color(0XFFFFFFFF).copy(0.8f),

    )



    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.TextOnBackground,
            textAlign = TextAlign.Start,
            style = appTextStyle(
                base = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
            ),
        )

        NumericInputContainer {

            var fieldValue by remember { mutableStateOf(TextFieldValue()) }

            LaunchedEffect(value) {
                if (fieldValue.text != value) {
                    fieldValue = TextFieldValue(
                        text = value,
                        selection = TextRange(value.length),
                    )
                }
            }

            OutlinedTextField(

                value = fieldValue,

                onValueChange = { updated ->
                    val digits = updated.text.filter { it.isDigit() }
                    fieldValue = TextFieldValue(
                        text = digits,
                        selection = TextRange(digits.length),
                    )
                    onValueChange(digits)
                },

                modifier = Modifier.fillMaxWidth(),

                isError = hasError,

                supportingText = errorMessage?.let { message ->

                    {

                        Text(
                            text = message,
                            color = Color(0xFFFF5252),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            style = appTextStyle(
                                base = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                            ),
                        )

                    }

                },

                placeholder = {
                    Text(
                        text = placeholder,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = amountInputTextStyle(fontSizeSp = 16),
                    )
                },

                leadingIcon = {

                    Image(

                        painter = painterResource(iconRes),

                        contentDescription = null,

                        colorFilter= ColorFilter.tint(if(hasError)

                            Color(0xFFFF5252) else Color(0XFF5FFBF3)),

                        modifier = Modifier.size(24.dp),

                    )

                },

                visualTransformation = { annotatedString ->

                    priceFilter(annotatedString.text)

                },

                shape = fieldShape,

                colors = fieldColors,

                singleLine = true,

                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),

                textStyle = amountInputTextStyle(),

            )

        }

    }

}



fun priceFilter(

    text: String,

    thousandSeparator: (String) -> String = { value -> value.withThousands() },

): TransformedText {

    val formatted = thousandSeparator(text)

    val offsetMapping = object : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 0) return 0
            val commas = (offset - 1) / 3
            return (offset + commas).coerceAtMost(formatted.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 0) return 0
            var original = 0
            var transformed = 0
            while (transformed < offset && original < text.length) {
                if (transformed < formatted.length && formatted[transformed] == ',') {
                    transformed++
                } else {
                    transformed++
                    original++
                }
            }
            return original.coerceAtMost(text.length)
        }

    }

    return TransformedText(AnnotatedString(formatted), offsetMapping)

}

fun cardNumberFilter(text: String): TransformedText {
    val formatted = text.chunked(4).joinToString(" ")
    val offsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 0) return 0
            val spaces = (offset - 1) / 4
            return (offset + spaces).coerceAtMost(formatted.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 0) return 0
            var original = 0
            var transformed = 0
            while (transformed < offset && original < text.length) {
                if (transformed < formatted.length && formatted[transformed] == ' ') {
                    transformed++
                } else {
                    transformed++
                    original++
                }
            }
            return original.coerceAtMost(text.length)
        }
    }
    return TransformedText(AnnotatedString(formatted), offsetMapping)
}



fun String.withThousands(separator: Char = ','): String {

    if (isEmpty()) return this

    return reversed()

        .chunked(3)

        .joinToString(separator.toString())

        .reversed()

}


