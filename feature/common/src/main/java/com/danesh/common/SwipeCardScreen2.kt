package com.danesh.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.danesh.ui.theme.appScreenBackground
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeCardScreen2() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            TopAppBar(
                title = {
                    Text(
                        "کشیدن کارت",
                        color = Color.White
                    )
                },
                navigationIcon = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text = "کارت خود را بکشید...",
                color = Color(0xFF58FFF6),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(40.dp))

            Image(
                painter = painterResource(com.danesh.ui.R.drawable.swipe_card),
                contentDescription = null,
                modifier = Modifier
                    .size(320.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color(0xFF58FFF6),
                    modifier = Modifier.size(40.dp)
                )

                NfcBottomCard()
            }
        }
    }
}
class TopCenterNotchShape : androidx.compose.ui.graphics.Shape {



    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = androidx.compose.ui.graphics.Path().apply {

            moveTo(0f, 40f)

            quadraticBezierTo(
                size.width * .25f,
                0f,
                size.width * .40f,
                0f
            )

            cubicTo(
                size.width * .45f,
                0f,
                size.width * .45f,
                30f,
                size.width * .50f,
                30f
            )

            cubicTo(
                size.width * .55f,
                30f,
                size.width * .55f,
                0f,
                size.width * .60f,
                0f
            )

            quadraticBezierTo(
                size.width * .75f,
                0f,
                size.width,
                40f
            )

            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        return Outline.Generic(path)
    }
}
@Composable
fun NfcBottomCard() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(TopCenterNotchShape())
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF012534),
                        Color(0xFF003847)
                    )
                )
            )
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "استفاده از NFC",
                color = Color(0xFF58FFF6),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "برای پرداخت از طریق NFC صفحه را بالا بکشید",
                color = Color.White,
                fontSize = 15.sp
            )
        }
//todo

//        Image(
//            painter = painterResource(R.drawable.nfc),
//            contentDescription = null,
//            modifier = Modifier.size(100.dp)
//        )
    }
}