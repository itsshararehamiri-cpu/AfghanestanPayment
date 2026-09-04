package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.settings.R
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun ConfigurationScreen(
    onBackClick: () -> Unit,
    onKeyLoadingClick: () -> Unit = {},
    onInitialConfigurationClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_configuration_title),
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
//            SettingsSectionTitle(
//                title = stringResource(R.string.settings_configuration_section_title),
//            )

            SettingsNavigationRow(
                label = stringResource(R.string.settings_key_loading),
                icon = R.drawable.ic_unlock,
                iconContentDescription = stringResource(R.string.settings_key_loading),
                onClick = onKeyLoadingClick,
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavigationRow(
                label = stringResource(R.string.settings_initial_configuration),
                icon = R.drawable.ic_configuration,
                iconContentDescription = stringResource(R.string.settings_initial_configuration),
                onClick = onInitialConfigurationClick,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun ConfigurationScreenPreview() {
    ConfigurationScreen(
        onBackClick = {},
        onInitialConfigurationClick = {},
    )
}
