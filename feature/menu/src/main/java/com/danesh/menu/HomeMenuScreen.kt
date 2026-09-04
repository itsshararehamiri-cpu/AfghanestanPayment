package com.danesh.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.common.ui.NetworkErrorScreen
import com.danesh.menu.model.MenuItemType
import com.danesh.menu.model.homeMenuItems
import com.danesh.menu.ui.ConfigurationRequiredBanner
import com.danesh.menu.ui.ConfigurationRequiredDialog
import com.danesh.menu.ui.MenuGrid
import com.danesh.menu.ui.MenuHeader
import com.danesh.ui.theme.appScreenBackground

@Composable
fun HomeMenuScreen(
    terminalId: String = "",
    merchantName: String = "",
    qrContent: String = "",
    menuItems: List<MenuItemType> = homeMenuItems,
    isConfigured: Boolean = true,
    onMenuItemClick: (MenuItemType) -> Unit ,
    onQrClick: () -> Unit ,
    onConfigurationClick: () -> Unit ,
    isConnected: Boolean = true,
    isWifiEnabled: () -> Boolean = { true },
    onRefreshNetwork: () -> Unit ,
    onOpenWifiSettings: () -> Unit,
) {
    var showConfigurationDialog by rememberSaveable { mutableStateOf(false) }
    var showNetworkError by rememberSaveable { mutableStateOf(false) }
    val resolvedQr = qrContent.ifBlank { "terminal:$terminalId" }
    val configurationTitle = stringResource(R.string.menu_configuration_required_title)
    val configurationBlockedMessage = stringResource(R.string.menu_configuration_blocked_message)

    LaunchedEffect(isConnected) {
        if (isConnected) {
            showNetworkError = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        MenuHeader(
            terminalId = terminalId,
            merchantName = merchantName,
            qrContent = resolvedQr,
            onQrClick = onQrClick,
        )

        Spacer(modifier = Modifier.height(24.dp))
        if (!isConfigured) {
            ConfigurationRequiredBanner(
                onSetupClick = onConfigurationClick,
            )

            Spacer(Modifier.height(20.dp))
        }

        MenuGrid(
            onMenuItemClick = { item ->
                if (!isConfigured && item.requiresTerminalConfiguration) {
                    showConfigurationDialog = true
                } else if (item.requiresNetwork && !isConnected) {
                    showNetworkError = true
                } else {
                    onMenuItemClick(item)
                }
            },
            items = menuItems,
        )

        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(R.drawable.adver), contentDescription = "",
            modifier = Modifier
                .padding(horizontal = 1.dp)
                .fillMaxWidth()
                .height(145.dp)
                .border(
                    1.dp, brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0XFF012C39),
                            Color(0XFF154857)
                        )
                    ),
                    RoundedCornerShape(15.dp)
                )
                .clip(RoundedCornerShape(15.dp)),
            contentScale = ContentScale.FillBounds
        )
    }

    if (showNetworkError) {
        val wifiEnabled = isWifiEnabled()
        NetworkErrorScreen(
            title = stringResource(com.danesh.common.R.string.network_error_title),
            message = stringResource(
                if (wifiEnabled) {
                    com.danesh.common.R.string.network_error_transaction_blocked
                } else {
                    com.danesh.common.R.string.network_error_wifi_disabled
                },
            ),
            primaryButtonText = stringResource(
                if (wifiEnabled) {
                    com.danesh.common.R.string.action_retry
                } else {
                    com.danesh.common.R.string.network_error_enable_wifi
                },
            ),
            onPrimaryClick = {
                if (wifiEnabled) {
                    onRefreshNetwork()
                } else {
                    onOpenWifiSettings()
                }
            },
            onCancelClick = { showNetworkError = false },
        )
    }
    }

    if (showConfigurationDialog) {
        ConfigurationRequiredDialog(
            title = configurationTitle,
            message = configurationBlockedMessage,
            onDismissRequest = { showConfigurationDialog = false },
            onSetupClick = {
                showConfigurationDialog = false
                onConfigurationClick()
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF002330)
@Composable
private fun HomeMenuScreenPreview() {
    HomeMenuScreen(
        terminalId = "12533864",
        merchantName = "Merchant", onMenuItemClick = {}, onQrClick = {}, onConfigurationClick = {},
        onRefreshNetwork = {}, onOpenWifiSettings = {}
    )
}
