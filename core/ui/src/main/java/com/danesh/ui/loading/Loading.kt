package com.danesh.ui.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Loading(modifier: Modifier=Modifier
    .fillMaxSize()
    .background(Color(0xFF022631)),color: Color) {
    Box(
        modifier =modifier ,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color =color, modifier = Modifier.align(Alignment.Center))
    }
}