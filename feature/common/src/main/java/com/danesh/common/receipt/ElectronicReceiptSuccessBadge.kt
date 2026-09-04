package com.danesh.common.receipt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danesh.common.R

@Composable
fun ElectronicReceiptSuccessBadge(
    modifier: Modifier = Modifier,isSuccess: Boolean,
//    glowSize: Dp = 120.dp,
//    badgeSize: Dp = 88.dp,
//    checkIconSize: Dp = 44.dp,
) {
    Box(
        contentAlignment = Alignment.Center
    ) {

//        Box(
//            modifier = Modifier
//                .size(180.dp)
//                .background(
//                    Brush.radialGradient(
//                        colors = listOf(
//                            Color(0xFFFFFFF),
//                            Color.Transparent
//                        )
//                    ),
//                    CircleShape
//                )
//                .blur(40.dp)
//        )

        Image(painter = painterResource(if(isSuccess)R.drawable.ic_succc else R.drawable.fail_logo), contentDescription = "",modifier= Modifier.size(100.dp))

    }


}

@Preview(showBackground = true, backgroundColor = 0xFF01242F)
@Composable
private fun PurchaseSuccessBadgePreview() {
    ElectronicReceiptSuccessBadge(isSuccess = true)
}