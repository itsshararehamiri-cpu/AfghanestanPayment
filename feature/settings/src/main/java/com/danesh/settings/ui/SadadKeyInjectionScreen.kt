package com.danesh.settings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.settings.R
import com.danesh.settings.domain.StartupStepResult
import com.danesh.settings.model.SadadKeyInjectionStep
import com.danesh.settings.model.SadadKeyInjectionUiState
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.button.OutlinedActionButton
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

private val ErrorColor = Color(0xFFFF8A80)

@Composable
fun SadadKeyInjectionScreen(
    uiState: SadadKeyInjectionUiState,
    onBackClick: () -> Unit,
    onRetryWaitClick: () -> Unit,
    onCardAIndexChange: (String) -> Unit,
    onCardCIndexChange: (String) -> Unit,
    onCardAPinChange: (String) -> Unit,
    onCardCPinChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    // حین تزریق کلید/INIT/LOGON خروج مجاز نیست تا PED و کارت در وضعیت نیمه‌کاره نمانند.
    val isBusy = uiState.step == SadadKeyInjectionStep.PROCESSING
    BackHandler(enabled = true) { if (!isBusy) onBackClick() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_sadad_wizard_title),
            onBackClick = { if (!isBusy) onBackClick() },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp),
        ) {
            when (uiState.step) {
                SadadKeyInjectionStep.WAIT_CARD -> WaitCardContent(uiState, onRetryWaitClick, onBackClick)
                SadadKeyInjectionStep.FORM -> FormContent(
                    uiState = uiState,
                    onCardAIndexChange = onCardAIndexChange,
                    onCardCIndexChange = onCardCIndexChange,
                    onCardAPinChange = onCardAPinChange,
                    onCardCPinChange = onCardCPinChange,
                    onSubmitClick = onSubmitClick,
                    onCancelClick = onBackClick,
                )
                SadadKeyInjectionStep.PROCESSING -> ProgressContent(
                    title = null,
                    message = uiState.statusMessage,
                )
                SadadKeyInjectionStep.SWAP_CARD -> ProgressContent(
                    title = stringResource(R.string.settings_sadad_swap_card_title),
                    message = uiState.statusMessage,
                )
                SadadKeyInjectionStep.SUCCESS -> SuccessContent(uiState, onBackClick)
                SadadKeyInjectionStep.ERROR -> ErrorContent(uiState, onRetryClick, onBackClick)
            }
        }
    }
}

@Composable
private fun WaitCardContent(
    uiState: SadadKeyInjectionUiState,
    onRetryClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Headline(stringResource(R.string.settings_sadad_insert_card_title))
    Spacer(modifier = Modifier.height(12.dp))
    val failure = uiState.waitFailedMessage
    if (failure == null) {
        Body(uiState.statusMessage.orEmpty())
        Spacer(modifier = Modifier.height(28.dp))
        Spinner()
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedActionButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.settings_sadad_cancel),
            onClick = onCancelClick,
        )
    } else {
        Body(failure, color = ErrorColor)
        Spacer(modifier = Modifier.height(24.dp))
        GradientActionButton(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            text = stringResource(R.string.settings_sadad_retry),
            onClick = onRetryClick,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedActionButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.settings_sadad_back),
            onClick = onCancelClick,
        )
    }
}

@Composable
private fun FormContent(
    uiState: SadadKeyInjectionUiState,
    onCardAIndexChange: (String) -> Unit,
    onCardCIndexChange: (String) -> Unit,
    onCardAPinChange: (String) -> Unit,
    onCardCPinChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    uiState.statusMessage?.let {
        Body(it, color = SettingsColors.Accent)
        Spacer(modifier = Modifier.height(16.dp))
    }
    SettingsTextField(
        value = uiState.cardAIndex,
        onValueChange = onCardAIndexChange,
        label = stringResource(R.string.settings_sadad_index_a_label),
        placeholder = stringResource(R.string.settings_sadad_index_placeholder),
        errorMessage = uiState.cardAIndexError,
        inputFilter = SettingsTextInputFilter.PositiveInteger,
    )
    Spacer(modifier = Modifier.height(12.dp))
    SettingsTextField(
        value = uiState.cardCIndex,
        onValueChange = onCardCIndexChange,
        label = stringResource(R.string.settings_sadad_index_c_label),
        placeholder = stringResource(R.string.settings_sadad_index_placeholder),
        errorMessage = uiState.cardCIndexError,
        inputFilter = SettingsTextInputFilter.PositiveInteger,
    )
    Spacer(modifier = Modifier.height(12.dp))
    SettingsTextField(
        value = uiState.cardAPin,
        onValueChange = onCardAPinChange,
        label = stringResource(R.string.settings_sadad_pin_a_label),
        placeholder = stringResource(R.string.settings_sadad_pin_placeholder),
        errorMessage = uiState.cardAPinError,
        inputFilter = SettingsTextInputFilter.DigitsOnly,
        isSecret = true,
    )
    Spacer(modifier = Modifier.height(12.dp))
    SettingsTextField(
        value = uiState.cardCPin,
        onValueChange = onCardCPinChange,
        label = stringResource(R.string.settings_sadad_pin_c_label),
        placeholder = stringResource(R.string.settings_sadad_pin_placeholder),
        errorMessage = uiState.cardCPinError,
        inputFilter = SettingsTextInputFilter.DigitsOnly,
        isSecret = true,
    )
    Spacer(modifier = Modifier.height(24.dp))
    GradientActionButton(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        text = stringResource(R.string.settings_sadad_start),
        onClick = onSubmitClick,
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedActionButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.settings_sadad_cancel),
        onClick = onCancelClick,
    )
}

@Composable
private fun ProgressContent(title: String?, message: String?) {
    title?.let {
        Headline(it)
        Spacer(modifier = Modifier.height(12.dp))
    }
    message?.let { Body(it) }
    Spacer(modifier = Modifier.height(28.dp))
    Spinner()
}

@Composable
private fun SuccessContent(uiState: SadadKeyInjectionUiState, onDoneClick: () -> Unit) {
    Headline(stringResource(R.string.settings_sadad_success_title), color = SettingsColors.Accent)
    Spacer(modifier = Modifier.height(12.dp))
    Body(
        text = uiState.statusMessage.orEmpty(),
        color = if (uiState.cardRemoved) SettingsColors.TextPrimary else SettingsColors.Accent,
        bold = !uiState.cardRemoved,
    )

    uiState.notes.forEach { note ->
        Spacer(modifier = Modifier.height(8.dp))
        Body(note)
    }

    uiState.kcv?.let { kcv ->
        Spacer(modifier = Modifier.height(20.dp))
        SettingsSectionTitle(title = stringResource(R.string.settings_sadad_kcv_title))
        Spacer(modifier = Modifier.height(8.dp))
        KeyLoadingKcvTable(kcvSummary = kcv)
    }

    Spacer(modifier = Modifier.height(20.dp))
    StepResultLine(stringResource(R.string.settings_sadad_result_init), uiState.initResult)
    if (uiState.initResult?.isSuccess == true) {
        Spacer(modifier = Modifier.height(8.dp))
        StepResultLine(stringResource(R.string.settings_sadad_result_logon), uiState.logonResult)
    }
    val startupFailed = uiState.initResult?.isSuccess != true || uiState.logonResult?.isSuccess != true
    if (startupFailed) {
        Spacer(modifier = Modifier.height(8.dp))
        Body(stringResource(R.string.settings_sadad_after_key_failure_hint))
    }

    uiState.printErrorMessage?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Body(it, color = ErrorColor)
    }

    Spacer(modifier = Modifier.height(24.dp))
    GradientActionButton(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        text = stringResource(R.string.settings_sadad_done),
        onClick = onDoneClick,
    )
}

@Composable
private fun ErrorContent(
    uiState: SadadKeyInjectionUiState,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Body(uiState.errorMessage.orEmpty(), color = ErrorColor, bold = true)
    Spacer(modifier = Modifier.height(24.dp))
    GradientActionButton(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        text = stringResource(R.string.settings_sadad_retry),
        onClick = onRetryClick,
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedActionButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.settings_sadad_back),
        onClick = onBackClick,
    )
}

@Composable
private fun StepResultLine(label: String, result: StartupStepResult?) {
    if (result == null) return
    Body(
        text = "$label: ${result.message}",
        color = if (result.isSuccess) SettingsColors.Accent else ErrorColor,
    )
}

@Composable
private fun Headline(text: String, color: Color = SettingsColors.TextPrimary) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        color = color,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun Body(text: String, color: Color = SettingsColors.TextPrimary, bold: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        color = color,
        fontSize = 14.sp,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun Spinner() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.size(44.dp), color = SettingsColors.Accent)
    }
}
