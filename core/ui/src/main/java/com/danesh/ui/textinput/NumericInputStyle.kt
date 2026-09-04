package com.danesh.ui.textinput



import androidx.compose.runtime.Composable

import androidx.compose.runtime.CompositionLocalProvider

import androidx.compose.ui.platform.LocalLayoutDirection

import androidx.compose.ui.text.TextStyle

import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.text.style.TextDirection

import androidx.compose.ui.unit.LayoutDirection

import androidx.compose.ui.unit.sp

import com.danesh.ui.theme.appFontFamily



fun KeyboardType.isNumericInput(): Boolean = when (this) {

    KeyboardType.Number,

    KeyboardType.Decimal,

    KeyboardType.Phone,

    KeyboardType.NumberPassword -> true

    else -> false

}



@Composable
fun numericInputTextStyle(fontSizeSp: Int = 16): TextStyle = TextStyle(
    fontFamily = appFontFamily(),
    textAlign = TextAlign.Start,
    textDirection = TextDirection.Ltr,
    fontSize = fontSizeSp.sp,
)

@Composable
fun amountInputTextStyle(fontSizeSp: Int = 16): TextStyle = numericInputTextStyle(fontSizeSp)



@Composable

fun NumericInputContainer(content: @Composable () -> Unit) {

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

        content()

    }

}



@Composable

fun NumericOrPlainInputContainer(

    keyboardType: KeyboardType,

    content: @Composable () -> Unit,

) {

    if (keyboardType.isNumericInput()) {

        NumericInputContainer(content)

    } else {

        content()

    }

}



@Composable

fun textFieldStyleForKeyboard(

    keyboardType: KeyboardType,

    fontSizeSp: Int = 16,

): TextStyle = if (keyboardType.isNumericInput()) {

    numericInputTextStyle(fontSizeSp)

} else {

    TextStyle(

        fontFamily = appFontFamily(),

        textAlign = TextAlign.End,

        fontSize = fontSizeSp.sp,

    )

}


