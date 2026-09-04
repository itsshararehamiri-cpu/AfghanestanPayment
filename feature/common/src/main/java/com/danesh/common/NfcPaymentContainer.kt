package com.danesh.common
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.presentation.viewmodel.SwipeCardEvent
import com.danesh.common.presentation.viewmodel.SwipeCardStatus
import com.danesh.common.presentation.viewmodel.SwipeCardViewModel
import com.danesh.ui.toolbar.Toolbar

@Composable
fun NfcPaymentContainer(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = Color(0xFF00222B), // رنگ پس‌زمینه تیره
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp) // گرد کردن لبه‌های بالا
            )
            .padding(24.dp), // فاصله داخلی
        verticalAlignment = Alignment.CenterVertically
    ) {
        // بخش متن‌ها
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "استفاده از NFC",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "برای پرداخت از طریق NFC صفحه را بالا بکشید",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        // بخش آیکون NFC
        Image(
            painter = painterResource(id = R.drawable.nfc), // عکس آیکون خود را اینجا بدهید
            contentDescription = "NFC",
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}
@Composable
@Preview
fun A(){
    NfcPaymentContainer(Modifier.fillMaxWidth())
}