package com.danesh.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.ui.theme.AppColors
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

private val AccentColor = AppColors.Accent
private const val PIN_LENGTH = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetPasswordScreen(
    pinValue: String = "",
    title: String? = null,
    instruction: String? = null,
    instructionText: String = "",
    hintText: String? = null,
    hintColor: Color = AccentColor,
    showRetryHint: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    onPinValueChange: (String) -> Unit,
    onPinComplete: (String) -> Unit,
) {
    val screenTitle = title ?: stringResource(R.string.settings_login_title)
    val displayInstruction = when {
        showRetryHint && !hintText.isNullOrBlank() -> hintText
        !instruction.isNullOrBlank() -> instruction
        instructionText == "Support" -> stringResource(R.string.settings_supervisor_password_instruction)
        instructionText == "Merchant" -> stringResource(R.string.settings_merchant_password_instruction)
        else -> stringResource(R.string.settings_password_instruction)
    }
    val isError = showRetryHint && !hintText.isNullOrBlank()

    val handleCancelClick = {
        if (pinValue.isNotEmpty()) {
            onPinValueChange("")
        } else {
            onCancelClick?.invoke() ?: onBackClick?.invoke()
        }
    }

    BackHandler(enabled = onBackClick != null || onCancelClick != null) {
        handleCancelClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(
                title = screenTitle,
                onBackClick = onBackClick,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = stringResource(R.string.password_shield_icon),
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                        .fillMaxWidth(0.78f)
                        .height(191.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Text(
                text = if (isError) hintText.orEmpty() else displayInstruction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = if (isError) hintColor else AppColors.TextOnBackground,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )

            if (showRetryHint && !isError) {
                Text(
                    text = hintText ?: stringResource(R.string.pin_retry),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    color = hintColor,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PasswordPinDots(
                pinValue = pinValue,
                pinLength = PIN_LENGTH,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            PasswordNumericKeypad(
                pinValue = pinValue,
                pinLength = PIN_LENGTH,
                onDigitClick = { digit ->
                    if (pinValue.length < PIN_LENGTH) {
                        val newValue = pinValue + digit
                        onPinValueChange(newValue)
                        if (newValue.length == PIN_LENGTH) {
                            onPinComplete(newValue)
                        }
                    }
                },
                onClearClick = {
                    if (pinValue.isNotEmpty()) {
                        onPinValueChange(pinValue.dropLast(1))
                    }
                },
                onCancelClick = {
                    handleCancelClick()
                },
                onConfirmClick = {
                    if (pinValue.length == PIN_LENGTH) {
                        onPinComplete(pinValue)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun GetPasswordScreenPreview() {
    GetPasswordScreen(pinValue = "12", instructionText = "", onPinValueChange = {}, onPinComplete = {}, onBackClick = {})
}
