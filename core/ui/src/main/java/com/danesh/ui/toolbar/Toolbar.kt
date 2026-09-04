package com.danesh.ui.toolbar

import android.util.Log
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.danesh.ui.R
import com.danesh.ui.theme.AppColors
import com.danesh.ui.theme.appTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar(
    title: String,
    onBackClick: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                color = AppColors.TextOnBackground,
                fontWeight = FontWeight.SemiBold,
                style = appTextStyle(
                    base = MaterialTheme.typography.bodySmall,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = {
                    onBackClick()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_white_arrow_to_right),
                        contentDescription = stringResource(R.string.label_back),
                        tint = AppColors.TextOnBackground,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppColors.ScreenBackground,
        ),
    )
}