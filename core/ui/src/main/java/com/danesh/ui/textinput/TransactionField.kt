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

import androidx.compose.ui.Modifier

import com.danesh.ui.theme.AppColors
import com.danesh.ui.theme.appTextStyle

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.ColorFilter

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp



@Composable

 fun TransactionField(

    label: String,

    value: String,

    onValueChange: (String) -> Unit,

    placeholder: String,

    iconRes: Int,

    keyboardType: KeyboardType,

    errorMessage: String? = null,

    visualTransformation: VisualTransformation = VisualTransformation.None,

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

        NumericOrPlainInputContainer(keyboardType = keyboardType) {

            OutlinedTextField(

                value = value,

                onValueChange = onValueChange,

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
                        style = textFieldStyleForKeyboard(keyboardType, fontSizeSp = 14),
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

                shape = fieldShape,

                colors = fieldColors,

                singleLine = true,

                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),

                visualTransformation = visualTransformation,

                textStyle = textFieldStyleForKeyboard(keyboardType),

            )

        }

    }

}


