package com.danesh.menu.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.receipt.LocalReceiptPspBrand
import com.danesh.common.receipt.ReceiptPspBrand
import com.danesh.common.receipt.brandLogoRes
import com.danesh.menu.R
import com.danesh.menu.model.MenuItemType
import com.danesh.menu.model.homeMenuItems
import com.danesh.menu.ui.theme.MenuColors

private val menuCardShape = RoundedCornerShape(14.dp)

@Composable
fun MenuHeader(
    terminalId: String,
    merchantName: String,
    qrContent: String,
    onQrClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val qrBitmap = rememberMenuQrCodeBitmap(qrContent)
    val pspBrand = LocalReceiptPspBrand.current
    val brandLogoRes = pspBrand.brandLogoRes()
    Row(
        modifier = modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0XFF002531).copy(0.36f),
                        Color(0XFF014355).copy(0.28f),
                        Color(0XFF01323D),
                        Color(0XFF012431), Color(0XFF07212F)
                    )
                )
            )
            .border(
                1.dp, brush = Brush.linearGradient(
                    listOf(Color(0XFF012C39), Color(0XFF154857))
                ), shape = RoundedCornerShape(10.dp)
            ),

        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {


        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(brandLogoRes),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 25.dp, start = 18.dp)
                        .size(70.dp),
                    contentScale = ContentScale.FillBounds// TODO: 28
                )
//                Text(
//                    text = stringResource(brandNameRes),
//                    color = MenuColors.TextPrimary,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    style = MaterialTheme.typography.bodySmall,
//                )
            }

            if (merchantName.isNotBlank()) {
                Text(
                    text = merchantName,
                    color = MenuColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 18.dp, top = 8.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                Text(
                    text = stringResource(R.string.menu_terminal_label),
                    color = MenuColors.TextSecondary,
                    fontSize = 13.sp,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 18.dp)
                )
                Text(
                    text = terminalId,
                    color = MenuColors.TerminalValue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxHeight()
                .width(150.dp)
                .clip(RoundedCornerShape(12.dp))
                //     .border(2.dp, MenuColors.Accent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .clickable(onClick = onQrClick)
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {

            when (pspBrand) {
                ReceiptPspBrand.BP -> {
                }

                else ->
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.menu_qr_content_description),
                            modifier = Modifier.size(80.dp),
                        )
                    }
            }
        }
    }


}

@Composable
fun MenuGrid(
    onMenuItemClick: (MenuItemType) -> Unit,
    modifier: Modifier = Modifier,
    items: List<MenuItemType> = homeMenuItems,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { item ->
                    MenuGridItem(
                        item = item,
                        onClick = { onMenuItemClick(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ConfigurationRequiredBanner(
    modifier: Modifier = Modifier,
    onSetupClick: () -> Unit,
) {
    val title = stringResource(R.string.menu_configuration_required_title)
    val description = stringResource(R.string.menu_configuration_required_description)
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFF1A2B32))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFFB74D),
                        Color(0xFFE6A23C)
                    )
                ),
                shape = shape
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0x22FFB74D)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = Color(0xFFFFB74D),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = description,
                color = Color.White.copy(alpha = .8f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        OutlinedButton(
            onClick = onSetupClick,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFFFB74D)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFFFFB74D)
            )
        ) {
            Text(stringResource(R.string.menu_configuration_setup_action))
        }
    }
}

@Composable
fun ConfigurationRequiredDialog(
    title: String,
    message: String,
    onDismissRequest: () -> Unit,
    onSetupClick: () -> Unit,
    setupActionLabel: String = stringResource(R.string.menu_configuration_setup_action),
    dismissActionLabel: String = stringResource(R.string.menu_configuration_dialog_dismiss),
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = Color(0xFFFFB74D),
            )
        },
        title = {
            Text(
                text = title,
                color = MenuColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = message,
                color = MenuColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )
        },
        confirmButton = {
            OutlinedButton(
                onClick = onSetupClick,
                border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFFB74D),
                ),
            ) {
                Text(setupActionLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = dismissActionLabel,
                    color = MenuColors.TextSecondary,
                )
            }
        },
        containerColor = Color(0xFF0E2632),
        titleContentColor = MenuColors.TextPrimary,
        textContentColor = MenuColors.TextSecondary,
        iconContentColor = Color(0xFFFFB74D),
    )
}

@Composable
private fun MenuGridItem(
    item: MenuItemType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(120.dp)
            .clip(menuCardShape)
            .background(Color(0XFF0E2632).copy(alpha = 0.55f))
            .border(1.dp, MenuColors.CardBorder, menuCardShape)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color(0x40BFBFBF),
                spotColor = Color(0x40BFBFBF)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(item.iconRes),
            contentDescription = null,
            modifier = Modifier.size(52.dp),
        )
        Text(
            text = stringResource(item.labelRes),
            color = MenuColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 2,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}