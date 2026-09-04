package com.danesh.common

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.ui.theme.appTextStyle

private val KeypadAccent = Color(0xFF5FFBF3)
private val KeypadBorder = Color(0xFF20C5CA)
private val KeypadBackground = Color(0xFF0C2C36)
private val KeypadCancelColor = Color(0xFFFF4F52)
private val KeypadShape = RoundedCornerShape(14.dp)
private val PinDotsContainerShape = RoundedCornerShape(12.dp)

private val keypadRows = listOf(
    listOf('3', '2', '1'),
    listOf('6', '5', '4'),
    listOf('9', '8', '7'),
)

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
fun PasswordPinDots(
    pinValue: String,
    pinLength: Int = 4,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth().drawBehind {

                    drawIntoCanvas {

                        val paint = Paint().asFrameworkPaint().apply {

                            color = android.graphics.Color.parseColor("#8020C5CA")

                            maskFilter =
                                BlurMaskFilter(
                                    10f,
                                    BlurMaskFilter.Blur.NORMAL
                                )
                        }

                        it.nativeCanvas.drawRoundRect(
                            0f,
                            0f,
                            size.width,
                            size.height,
                            16.dp.toPx(),
                            16.dp.toPx(),
                            paint
                        )
                    }
                }
              /*  .drawBehind {
                    drawIntoCanvas { canvas ->

                        val paint = Paint().asFrameworkPaint().apply {
                            color = android.graphics.Color.parseColor("#20C5CA")
                            maskFilter = android.graphics.BlurMaskFilter(
                                0.1f,
                                android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                        }
                        val blur = 4.dp.toPx()

                        canvas.nativeCanvas.drawRoundRect(
                            -blur / 2,
                            -blur / 2,
                            size.width + blur / 2,
                            size.height + blur / 2,
                            20.dp.toPx(),
                            20.dp.toPx(),
                            paint
                        )
//                        canvas.nativeCanvas.drawRoundRect(
//                            0f,
//                            0f,
//                            size.width,
//                            size.height,
//                            20.dp.toPx(),
//                            20.dp.toPx(),
//                            paint
//                        )
                    }
//                    drawRoundRect(
//                        color = Color(0x8020C5CA),
//                        cornerRadius = CornerRadius(20.dp.toPx()),
//                        style = Fill,
//                        alpha = 0.2f
//                    )
                }*/
                .clip(PinDotsContainerShape)
                .background(KeypadBackground.copy(alpha = 0.55f))
                .border(
                    1.dp,
                    KeypadBorder.copy(alpha = 0.9f),
                    PinDotsContainerShape
                )
                .padding(vertical = 18.dp, horizontal = 24.dp), contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pinLength) { index ->
                    val filled = index < pinValue.length
                    if (filled) {
                        Text(
                            text = "*",
                            color = KeypadAccent,
                            style = appTextStyle(
                                base = MaterialTheme.typography.titleMedium,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .border(
                                    1.5.dp,
                                    KeypadAccent.copy(alpha = 0.65f),
                                    CircleShape,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordNumericKeypad(
    pinValue: String,
    onDigitClick: (Char) -> Unit,
    onClearClick: () -> Unit,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    pinLength: Int = 4,
    showConfirmButton: Boolean = true,
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
            KeypadIconActionButton(
                iconRes = R.drawable.ic_clear,
                label = stringResource(R.string.password_keypad_clear),
                modifier = Modifier.weight(1f),
                onClick = onClearClick,
            )
            KeypadDigitButton(
                label = '0'.toPersianDigit(),
                modifier = Modifier.weight(1f),
                onClick = { onDigitClick('0') },
            )
            KeypadIconActionButton(
                iconRes = R.drawable.ic_cancel,
                label = stringResource(R.string.action_cancel),
                labelColor = KeypadCancelColor,
                modifier = Modifier.weight(1f),
                onClick = onCancelClick,
            )
        }

        if (showConfirmButton) {
            Spacer(modifier = Modifier.height(6.dp))
            KeypadConfirmButton(
                label = stringResource(R.string.password_keypad_confirm),
                enabled = pinValue.length == pinLength,
                onClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun KeypadDigitButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(KeypadShape)
            .background(KeypadBackground.copy(alpha = 0.85f))
            .border(1.dp, KeypadBorder, KeypadShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color(0xFF1AF7FF),
            style = appTextStyle(
                base = MaterialTheme.typography.titleMedium,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun KeypadIconActionButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = Color.White,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(KeypadShape)
            .background(KeypadBackground.copy(alpha = 0.85f))
            .border(1.dp, KeypadBorder, KeypadShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = label,
                color = labelColor,
                style = appTextStyle(
                    base = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

@Composable
private fun KeypadConfirmButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF00FFD4),
            Color(0xFF0E6268),
        ),
    )
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(KeypadShape)
            .then(
                if (enabled) {
                    Modifier
                        .background(gradient, KeypadShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick,
                        )
                } else {
                    Modifier
                        .background(KeypadBackground.copy(alpha = 0.5f), KeypadShape)
                        .border(1.dp, KeypadBorder.copy(alpha = 0.5f), KeypadShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.35f),
            style = appTextStyle(
                base = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}
