package com.danesh.coupon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.currency.amountWithCurrency
import com.danesh.common.receipt.formatAmount
import com.danesh.ui.theme.AppColors

internal val CouponCardShape = RoundedCornerShape(14.dp)
internal val CouponCardBackground = Color(0xFF000000).copy(alpha = 0.19f)
internal val CouponCardBorder = Color(0xFF144B5B)
internal val CouponMuted = Color(0xFFB0BEC5)

internal fun Modifier.couponCard(highlight: Boolean = false): Modifier = this
    .fillMaxWidth()
    .background(CouponCardBackground, CouponCardShape)
    .border(1.dp, if (highlight) AppColors.Accent else CouponCardBorder, CouponCardShape)

/** مبلغ ریالی با جداکننده و واحد پول. */
@Composable
internal fun rials(amount: Long): String = amountWithCurrency(amount.toString().formatAmount())

/** ردیف «عنوان ...... مقدار» برای خلاصهٔ مبالغ. */
@Composable
internal fun CouponSummaryRow(
    label: String,
    value: String,
    emphasize: Boolean = false,
    ltrValue: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = if (emphasize) AppColors.TextOnBackground else CouponMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            color = if (emphasize) AppColors.Accent else AppColors.TextOnBackground,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            fontSize = if (emphasize) 17.sp else 15.sp,
            style = MaterialTheme.typography.bodyMedium.let {
                if (ltrValue) it.copy(textDirection = TextDirection.Ltr) else it
            },
        )
    }
}

/** برچسب کوچک کنار نام کالا (مثلاً «تعدادی»). */
@Composable
internal fun CouponTag(text: String) {
    Text(
        text = text,
        color = AppColors.Accent,
        fontSize = 11.sp,
        modifier = Modifier
            .border(1.dp, AppColors.Accent.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
