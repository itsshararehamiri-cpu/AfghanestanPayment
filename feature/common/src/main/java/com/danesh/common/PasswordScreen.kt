package com.danesh.common

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(
    hintText: String? = null,
    errorMessage: String? = null,
    hintColor: Color = AccentColor,
    isReadingPin: Boolean = false,
    showRetryHint: Boolean = false,
    onBackClick: () -> Unit,
) {
    val primaryMessage = when {
        showRetryHint && !errorMessage.isNullOrBlank() -> errorMessage
        isReadingPin -> stringResource(R.string.pin_reading)
        !hintText.isNullOrBlank() -> hintText
        else -> stringResource(R.string.password_hint)
    }
    val primaryColor = when {
        showRetryHint -> hintColor
        isReadingPin -> AccentColor
        else -> AppColors.TextOnBackground
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(
                title = stringResource(R.string.password_title),
                onBackClick = onBackClick,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(com.danesh.ui.R.drawable.ic_secure_shield),
                    contentDescription = stringResource(R.string.password_shield_icon),
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .fillMaxWidth(0.72f),
                    contentScale = ContentScale.Fit,
                )
            }

            Text(
                text = primaryMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = primaryColor,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )

            if (showRetryHint) {
               onBackClick()
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun PasswordScreenPreview() {
//    PasswordScreen(isReadingPin = true)
}
