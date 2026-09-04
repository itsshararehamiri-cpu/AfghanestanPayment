package com.danesh.card_to_card.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.card_to_card.R
import com.danesh.card_to_card.model.CardToCardTransferDetails
import com.danesh.card_to_card.ui.theme.CardToCardColors

private val cardShape = RoundedCornerShape(16.dp)

@Composable
fun CardToCardConfirmCard(
    details: CardToCardTransferDetails,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0XFF002E3D),
                        Color(0XFF005562),
                        Color(0XFF002733),
                    ),
                ),
            )
            .border(1.dp, Color(0XFF1F4955), cardShape),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.card_to_card_transfer_to),
                color = White.copy(alpha = 0.76f),
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
            )

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0XFF1F4855).copy(0.06f),
                                Color(0XFF417889),
                                Color(0XFF20C5CA),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(details.recipientAvatarRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .border(2.dp, CardToCardColors.Background, CircleShape),
                )
            }

            Text(
                text = details.recipientName,
                color = Color(0XFF5FFBF3),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )

            CardToCardLabeledValue(
                label = stringResource(R.string.card_to_card_source_label),
                value = details.sourceCardNumber,
                ltrValue = true,
            )

            CardToCardLabeledValue(
                label = stringResource(
                    if (details.destinationIsWallet) {
                        R.string.card_to_card_destination_wallet_label
                    } else {
                        R.string.card_to_card_destination_card_label
                    },
                ),
                value = details.destinationNumber,
                ltrValue = true,
            )

            CardToCardDashedDivider()

            CardToCardAmountRow(
                amount = details.amount,
                currency = details.currency,
            )
        }
    }
}

@Composable
private fun CardToCardLabeledValue(
    label: String,
    value: String,
    ltrValue: Boolean = false,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            color = White.copy(alpha = 0.65f),
            fontSize = 12.sp,
            style = MaterialTheme.typography.bodySmall,
        )
        if (ltrValue) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        } else {
            Text(
                text = value,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CardToCardAmountRow(
    amount: String,
    currency: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.card_to_card_amount_label),
            color = Color.White.copy(0.76f),
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = amount,
            color = Color(0XFF5FFBF3),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = currency,
            color = Color.White.copy(0.76f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun CardToCardDashedDivider(modifier: Modifier = Modifier) {
    val dividerColor = CardToCardColors.Background.copy(alpha = 0.25f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = dividerColor,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
@Preview
fun CardToCardConfirmCardPreview() {
    CardToCardConfirmCard(
        details = CardToCardTransferDetails(
            recipientName = "عرفانه سلیمانی روزبهانی",
            sourceCardNumber = "5022 2915 5235 5602",
            destinationNumber = "6037 9912 3456 7890",
            amount = "10,000,000",
            currency = "AFN",
        ),
    )
}
