package com.danesh.settings.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danesh.settings.R
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.button.OutlinedActionButton

@Composable
fun SettingsEmptyStateContent(
    message: String,
    modifier: Modifier = Modifier,
    @DrawableRes illustrationRes: Int = R.drawable.ic_wifi_not_found,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(illustrationRes),
            contentDescription = null,
            modifier = Modifier.size(220.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(com.danesh.common.R.drawable.ic_unsucess),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = message,
                    color = Color(0xFFFF5B5B),
                    modifier = Modifier.padding(start = 12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun SettingsEmptyStateActions(
    @StringRes primaryButtonTextRes: Int,
    onPrimaryClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryIconRes: Int = com.danesh.ui.R.drawable.ic_refresh,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        GradientActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            icon = primaryIconRes,
            text = stringResource(primaryButtonTextRes),
            onClick = onPrimaryClick,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            text = stringResource(com.danesh.common.R.string.action_cancel),
            onClick = onCancelClick,
        )
    }
}
