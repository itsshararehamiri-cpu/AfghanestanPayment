package com.danesh.topup.ui
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.currency.currencyLabel
import com.danesh.topup.ChargeOperatorOption
import com.danesh.topup.ui.theme.TopUpColors
import com.danesh.ui.theme.appTextStyle

private val cardShape = RoundedCornerShape(12.dp)
private val selectedBorder = Brush.horizontalGradient(
    colors = listOf(White, White),
)

@Composable
fun OperatorSelectionRow(
    operators: List<ChargeOperatorOption>,
    selectedOperatorId: String?,
    onOperatorSelected: (ChargeOperatorOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        operators.forEach { operator ->
            OperatorCard(
                operator = operator,
                isSelected = operator.id == selectedOperatorId,
                onClick = { onOperatorSelected(operator) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun OperatorCard(
    operator: ChargeOperatorOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderModifier = if (isSelected) {
        Modifier.border(width = 1.5.dp, brush = selectedBorder, shape = cardShape)
    } else {
        Modifier.border(width = 1.dp, color = TopUpColors.Background, shape = cardShape)
    }

    Column(
        modifier = modifier
            .height(88.dp)
            .clip(cardShape)
            .background(TopUpColors.Background)
            .then(borderModifier)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(operator.logoRes),
            contentDescription = operator.displayName,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = operator.displayName,
            color = TopUpColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun ChargeGroupRow(
    groups: List<String>,
    selectedGroup: String?,
    onGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (groups.size <= 1) return
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        groups.forEach { group ->
            val selected = group == selectedGroup
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(cardShape)
                    .background(Color(0XFF000000).copy(alpha = 0.19f))
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) Color(0XFF00FFD4) else Color(0XFF144B5B),
                        shape = cardShape,
                    )
                    .clickable { onGroupSelected(group) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = group,
                    color = TopUpColors.TextPrimary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun AmountQuickSelectGrid(
    presetAmounts: List<Int>,
    selectedAmount: Int?,
    onAmountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        presetAmounts.chunked(3).forEach { row ->
            AmountQuickSelectRow(
                presetAmounts = row,
                selectedAmount = selectedAmount,
                onAmountSelected = onAmountSelected,
            )
        }
    }
}

@Composable
fun AmountQuickSelectRow(
    presetAmounts: List<Int>,
    selectedAmount: Int?,
    onAmountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        presetAmounts.forEach { amount ->
            AmountChip(
                amount = amount,
                isSelected = amount == selectedAmount,
                onClick = { onAmountSelected(amount) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AmountChip(
    amount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderModifier = if (isSelected) {
        Modifier.border(width = 1.5.dp, color = Color(0XFF00FFD4), shape = cardShape)
    } else {
        Modifier.border(width = 1.dp, color  = Color(0XFF144B5B), shape = cardShape)
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(cardShape)
            .background(Color(0XFF000000).copy(alpha = 0.19f))
            .then(borderModifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(Modifier.wrapContentWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatAmount(amount),
                color = TopUpColors.TextPrimary,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = appTextStyle(
                    base = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                ),
            )
            Text(
                text = currencyLabel(),
                color = TopUpColors.TextPrimary,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = appTextStyle(
                    base = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
