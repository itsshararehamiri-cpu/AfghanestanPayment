package com.danesh.purchase.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val BALANCE_SUCCESS_BADGE_TEST_TAG = "balance_success_badge"

private val RadialCircleCenter = Color(0xFF20C5CA)
private val RadialCircleMid = Color(0x41788900)
private val RadialCircleEdge = Color(0x1F48550F)

@Composable
fun IconContainer(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,icon:Int
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0XFF1F4855).copy(0.6f),
                        Color(0XFF417889),//
                        Color(0XFF20C5CA),
                    ),
                ),
                shape = CircleShape,
            ), contentAlignment = Alignment.Center
    ){
        Image(painter = painterResource(icon), modifier = Modifier
            .width(18.dp)
            .height(18.dp), contentDescription = "")
    }
}



@Preview(showBackground = true, backgroundColor = 0xFF01242F)
@Composable
private fun APreview() {
    //A()
}
