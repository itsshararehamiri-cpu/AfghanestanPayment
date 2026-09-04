package com.danesh.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.presentation.viewmodel.SwipeCardEvent
import com.danesh.common.presentation.viewmodel.SwipeCardStatus
import com.danesh.common.presentation.viewmodel.SwipeCardViewModel
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.button.OutlinedActionButton
import com.danesh.ui.toolbar.Toolbar

@Composable
fun InvalidCardScreen(
    message: String,
    onRetryClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0XFF022631))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Toolbar(stringResource(R.string.swipe_card_title)) {
            onCancelClick()
        }

        Spacer(Modifier.height(40.dp))

        Image(
            painter = painterResource(R.drawable.read_card_error),//invalid_card
            contentDescription = null, modifier = Modifier.size(260.dp)
        )

        Spacer(Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ), border = BorderStroke(
                1.dp, Color.White.copy(alpha = 0.2f)
            ), modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.ic_unsucess),
                    contentDescription = "",
                    modifier = Modifier.padding(horizontal = 5.dp).size(20.dp)
                )
                Text(
                    text = message,
                    color = Color(0xFFFF5B5B),
                    modifier = Modifier.padding(24.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(1f))




        GradientActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            icon = com.danesh.ui.R.drawable.ic_refresh,
            text = stringResource(R.string.action_retry),
            onClick = { onRetryClick() },
        )
        Spacer(Modifier.height(16.dp))


        OutlinedActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            text = stringResource(R.string.action_cancel),
            onClick = { onCancelClick() },
        )
    }
}