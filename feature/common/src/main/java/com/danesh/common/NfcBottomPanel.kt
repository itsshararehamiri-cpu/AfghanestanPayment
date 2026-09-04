package com.danesh.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun NfcBottomPanel(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val panelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
//    Box(modifier.fillMaxWidth().wrapContentHeight()) {
//        Image(
//            painter = painterResource(R.drawable.nfc_container),
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight().padding(top = 10.dp),
//            contentScale = ContentScale.Inside, contentDescription = ""
//        )
//        Row(
//            modifier = modifier
//                .clip(panelShape)
//                .background(Color(0xE6001418))
//                .border(
//                    width = 1.dp,
//                    brush = Brush.verticalGradient(
//                        colors = listOf(
//                            Color(0x3300E5FF),
//                            Color.Transparent,
//                        ),
//                    ),
//                    shape = panelShape,
//                )
//                .clickable(onClick = onClick)
//                .padding(horizontal = 20.dp, vertical = 20.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween,
//        ) {
//
//
//            NfcGlowIcon()
//            Spacer(modifier = Modifier.width(16.dp))
//
//            Column(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth(),
//                verticalArrangement = Arrangement.spacedBy(6.dp),
//            ) {
//                Text(
//                    text = stringResource(R.string.balance_nfc_title),
//                    color = Color(0XFF5FFBF3),
//                    fontSize = 17.sp,
//                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall
//                )
//                Text(
//                    text = stringResource(R.string.balance_nfc_description),
//                    color = Color(0XFFFFFFFF),
//                    fontSize = 13.sp,
//                    lineHeight = 20.sp, style = MaterialTheme.typography.bodySmall
//                )
//            }
//
//        }
//    }

    val myPathData = "M0,182H412V27.13C412,22.16 402.5,7 391.45,7H266.87C264.33,6.99 261.83,7.26 259.54,7.8C257.26,8.34 255.27,9.12 253.73,10.1L248.56,13.44C244.45,16.11 237.7,20.47 230.49,20.47H181.42C173.99,20.47 167.05,16.01 162.99,13.21L158.73,10.27C157.21,9.25 155.19,8.42 152.86,7.85C150.52,7.28 147.94,6.99 145.33,7H23C2.5,7 0,22.16 0,27.13L0,182Z"
    val customShape = remember { VectorPathShape(myPathData) }




    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        AsyncImage(
            model = R.drawable.nfc_container,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
                .height( 178.dp)

                .clip(customShape)
                .border(2.dp, color = Color(0XFF00D9FF).copy(alpha = 0.24f) , customShape)  .shadow(
                    elevation = 12.dp,
                    shape = customShape,
                    clip = false
                )
            /*
            brush = Brush.radialGradient(listOf(Color(0XFF013543),Color(0XFF043746)
                ,
                    Color(0XFF002531)))
             */
        )
//        Box {
//        Image(
//            painter = painterResource(R.drawable.nfc_container),
//            contentDescription = null,
//            modifier = Modifier.matchParentSize().background(
//                brush = Brush.radialGradient(listOf(Color(0XFF013543),
//                    Color(0XFF043746),
//                    Color(0XFF002531)))
//            ),
//            contentScale = ContentScale.FillBounds
//        )

        Row(
            modifier = modifier.padding(top = 40.dp)
                .clip(panelShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            NfcGlowIcon()

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.balance_nfc_title),
                    color = Color(0XFF5FFBF3),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = stringResource(R.string.balance_nfc_description),
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
       // }
    }}
}
@Composable
@Preview
fun
        NfcBottomPanelPreview(){
    NfcBottomPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp),
        onClick = {},
    )
        }