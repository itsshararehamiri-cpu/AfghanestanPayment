package com.danesh.coupon.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.CouponCatalog
import com.danesh.api.CouponProduct
import com.danesh.common.currency.amountInWordsWithCurrency
import com.danesh.coupon.R
import com.danesh.coupon.data.CouponCartLine
import com.danesh.coupon.presentation.CouponCartEvent
import com.danesh.coupon.presentation.CouponCartUiState
import com.danesh.coupon.presentation.CouponCartViewModel
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.textinput.AmountTransactionField
import com.danesh.ui.theme.AppColors
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun CouponCartRoute(
    onBackClick: () -> Unit,
    onInquirySucceeded: () -> Unit,
    onInquiryFailed: (response: String) -> Unit,
    viewModel: CouponCartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                CouponCartEvent.InquirySucceeded -> onInquirySucceeded()
                is CouponCartEvent.InquiryFailed -> onInquiryFailed(event.response)
            }
        }
    }
    BackHandler(enabled = !uiState.isLoading) { onBackClick() }
    CouponCartScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onAddProduct = viewModel::addProduct,
        onRemoveLine = viewModel::removeLine,
        onAmountChange = viewModel::onAmountChange,
        onCountChange = viewModel::onCountChange,
        onInquire = viewModel::inquire,
        onDismissMessage = viewModel::clearMessage,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CouponCartScreen(
    uiState: CouponCartUiState,
    onBackClick: () -> Unit,
    onAddProduct: (CouponProduct) -> Unit,
    onRemoveLine: (Long) -> Unit,
    onAmountChange: (Long, String) -> Unit,
    onCountChange: (Long, Int) -> Unit,
    onInquire: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().appScreenBackground()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(title = stringResource(R.string.coupon_title), onBackClick = onBackClick)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                SectionTitle(stringResource(R.string.coupon_choose_products))
                if (uiState.products.isEmpty()) {
                    Text(
                        text = stringResource(R.string.coupon_no_enabled_products),
                        color = CouponMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.products.forEach { product ->
                        val added = !product.isOther && uiState.lines.any { it.product.barcode == product.barcode }
                        val disabled = added || (product.isOther && !uiState.canAddOther)
                        ProductChip(
                            label = product.nameFa.ifBlank { product.nameEn } +
                                if (product.isOther) " (${uiState.otherCount}/${CouponCatalog.MAX_OTHER_ITEMS})" else "",
                            selected = added,
                            enabled = !disabled,
                            onClick = { onAddProduct(product) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle(stringResource(R.string.coupon_cart))
                if (uiState.lines.isEmpty()) {
                    Text(
                        text = stringResource(R.string.coupon_cart_hint),
                        color = CouponMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                uiState.lines.forEach { line ->
                    CartLineCard(
                        line = line,
                        hasError = line.id in uiState.invalidLineIds,
                        onRemove = { onRemoveLine(line.id) },
                        onAmountChange = { onAmountChange(line.id, it) },
                        onCountChange = { onCountChange(line.id, it) },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF011B28))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                CouponSummaryRow(
                    label = stringResource(R.string.coupon_total_amount),
                    value = rials(uiState.totalRials),
                    emphasize = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                GradientActionButton(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    text = stringResource(R.string.coupon_inquire),
                    onClick = onInquire,
                )
            }
        }
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AppColors.Accent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.coupon_inquiring), color = AppColors.TextOnBackground)
                }
            }
        }
    }
    uiState.message?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissMessage,
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onDismissMessage) { Text(stringResource(R.string.coupon_ok)) }
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = AppColors.TextOnBackground,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
    )
}

@Composable
private fun ProductChip(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .background(
                if (selected) AppColors.Accent.copy(alpha = 0.18f) else CouponCardBackground,
                shape,
            )
            .border(1.dp, if (selected) AppColors.Accent else CouponCardBorder, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = if (enabled) AppColors.Accent else CouponMuted,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = if (enabled || selected) AppColors.TextOnBackground else CouponMuted,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun CartLineCard(
    line: CouponCartLine,
    hasError: Boolean,
    onRemove: () -> Unit,
    onAmountChange: (String) -> Unit,
    onCountChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.couponCard().padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = line.product.nameFa.ifBlank { line.product.nameEn },
                color = AppColors.TextOnBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (line.product.countable) CouponTag(stringResource(R.string.coupon_tag_countable))
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.coupon_remove), tint = CouponMuted)
            }
        }
        AmountTransactionField(
            label = stringResource(
                if (line.product.countable) R.string.coupon_unit_amount else R.string.coupon_amount,
            ),
            value = line.amountDigits,
            onValueChange = onAmountChange,
            placeholder = stringResource(R.string.coupon_amount_placeholder),
            iconRes = com.danesh.ui.R.drawable.ic_coin,
            keyboardType = KeyboardType.Number,
            errorMessage = if (hasError) stringResource(R.string.coupon_amount_required) else null,
            amountInWords = amountInWordsWithCurrency(line.amountDigits),
        )
        if (line.product.countable) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.coupon_count),
                    color = CouponMuted,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onCountChange(line.count - 1) }, enabled = line.count > 1) {
                    Icon(Icons.Filled.Remove, contentDescription = null, tint = AppColors.Accent)
                }
                Text(
                    text = line.effectiveCount.toString(),
                    color = AppColors.TextOnBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                IconButton(onClick = { onCountChange(line.count + 1) }) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = AppColors.Accent)
                }
            }
            CouponSummaryRow(
                label = stringResource(R.string.coupon_line_total),
                value = rials(line.totalRials),
            )
        }
    }
}
