package com.danesh.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.common.receipt.LocalReceiptPspBrand
import com.danesh.common.receipt.brandLogoRes

@Composable
fun SplashScreen() {
    val brandLogoRes = LocalReceiptPspBrand.current.brandLogoRes()
    Box(
        modifier = Modifier
            .fillMaxSize().background(Color(0xFF022631)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = brandLogoRes),
            contentDescription = stringResource(R.string.content_desc_splash),
            modifier = Modifier
                .padding(48.dp)
                .size(220.dp).align(Alignment.Center),
            contentScale = ContentScale.FillBounds,
        )
    }
}
@Preview
@Composable
fun SplashPreview(){
    SplashScreen()
}
