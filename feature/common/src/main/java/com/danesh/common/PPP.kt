//package com.danesh.common
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.CompositionLocalProvider
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.drawBehind
//import androidx.compose.ui.geometry.CornerRadius
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Paint
//import androidx.compose.ui.graphics.drawscope.Fill
//import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
//import androidx.compose.ui.graphics.nativeCanvas
//import androidx.compose.ui.platform.LocalLayoutDirection
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.LayoutDirection
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//val Background = Color(0xFF042E36)
//
//val Cyan = Color(0xFF20F0F4)
//
//val Border = Color(0xAA22E8F0)
//
//val Card = Color(0x2228B7C0)
//
//val ButtonGradientStart = Color(0xFF14E7D6)
//val ButtonGradientEnd = Color(0xFF1C717A)
//@Composable
//fun PasswordScreen() {
//
//    Scaffold(
//        containerColor = Background
//    ) { padding ->
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(horizontal = 28.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//
//            TopBar()
//
//            Spacer(Modifier.height(18.dp))
//
//            Image(
//                painterResource(R.drawable.ic_lock),
//                null,
//                Modifier.size(150.dp)
//            )
//
//            Spacer(Modifier.height(18.dp))
//
//            Text(
//                "لطفا رمز عبور خود را وارد کنید",
//                color = Color.White
//            )
//
//            Spacer(Modifier.height(18.dp))
//
//            PasswordPinDots("12")
//
//            Spacer(Modifier.height(38.dp))
//
//            Keypad()
//
//            Spacer(Modifier.weight(1f))
//
//            ConfirmButton()
//
//            Spacer(Modifier.height(20.dp))
//        }
//    }
//}