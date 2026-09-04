package com.danesh.common.receipt

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.danesh.common.Dimensions.PSP_LOGO_hEIGHT_RECEPINT

@Composable
fun AddPSPLog(modifier: Modifier, color: Color, isPaperReceipt: Boolean) {
    when (LocalReceiptPspBrand.current) {
        ReceiptPspBrand.HP -> HamrahPayPspLogo(
            modifier = modifier,
            color = color,
            isPaperReceipt = isPaperReceipt,
        )
        else -> OtherPspLogo(
            modifier = modifier,
            color = color,
            isPaperReceipt = isPaperReceipt,
            brand = LocalReceiptPspBrand.current,
        )
    }
}

@Composable
private fun HamrahPayPspLogo(
    modifier: Modifier,
    color: Color,
    isPaperReceipt: Boolean,
) {
    val logos = ReceiptPspBrand.HP.receiptLogoAssets()
    val verticalPad = if (isPaperReceipt) 0.dp else 5.dp

    Row(
        modifier = modifier
            .padding(bottom = verticalPad, top = verticalPad)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = if(isPaperReceipt)logos.brandLogoPaper else logos.brandLogo),
            contentDescription = null,
            modifier = Modifier
                .height(
                    if (isPaperReceipt) 90.dp else PSP_LOGO_hEIGHT_RECEPINT,
                )
                .width(if (isPaperReceipt && logos.tintBrandOnPaper) 130.dp else 90.dp)
                .align(Alignment.CenterVertically),
            contentScale = ContentScale.FillBounds,
          // colorFilter = if (isPaperReceipt) ColorFilter.tint(Black) else null,
        )
    }
}

@Composable
private fun OtherPspLogo(
    modifier: Modifier,
    color: Color,
    isPaperReceipt: Boolean,
    brand: ReceiptPspBrand,
) {
    val logos = brand.receiptLogoAssets()

    if (isPaperReceipt) {
        Image(
            painter = painterResource(id =logos.brandLogoPaper),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 0.dp, top = 10.dp)
                .fillMaxWidth().height(55.dp),
            contentScale = ContentScale.Fit, //colorFilter = ColorFilter.tint(Black)
        )
    } else {
        Row(
            modifier = modifier
                .padding(bottom = 10.dp, top = 10.dp)
                .fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(id = logos.networkLogo),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 0.dp)
                    .height(PSP_LOGO_hEIGHT_RECEPINT)
                    .width(90.dp)
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.FillBounds,
            )

            Spacer(modifier = Modifier.weight(1f))

            if (brand == ReceiptPspBrand.PN) {
                Image(
                    painter = painterResource(id = logos.brandLogo),
                    contentDescription = null,
                    modifier = Modifier
                        .height(PSP_LOGO_hEIGHT_RECEPINT)
                        .width(130.dp)
                        .align(Alignment.CenterVertically),
                )
            } else {
                Image(
                    painter = painterResource(id = logos.brandLogo),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 0.dp)
                        .height(PSP_LOGO_hEIGHT_RECEPINT)
                        .width(130.dp)
                        .align(Alignment.CenterVertically),
                    contentScale = ContentScale.FillWidth,
                )
            }
        }
    }
}
