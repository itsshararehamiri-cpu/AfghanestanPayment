package com.danesh.voucher

import androidx.lifecycle.ViewModel
import com.danesh.api.ChargeCatalog
import com.danesh.api.ChargeKind
import com.danesh.api.displayName
import com.danesh.common.strings.AppStrings
import com.danesh.common.locale.AppLanguage
import com.danesh.common.locale.LocalePreferences
import com.danesh.voucher.model.ChargeOperatorOption
import com.danesh.voucher.model.MobileOperator
import com.danesh.voucher.model.operatorCode
import com.danesh.voucher.model.sadadOperatorLogo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class VoucherUiState(
    val operators: List<ChargeOperatorOption> = emptyList(),
    val selectedOperatorId: String? = null,
    val presetAmounts: List<Int> = emptyList(),
    val selectedAmount: Int? = null,
    val amountText: String = "",
    val operatorError: String? = null,
    val amountError: String? = null,
)

@HiltViewModel
class VoucherViewModel @Inject constructor(
    private val appStrings: AppStrings,
    private val chargeCatalog: ChargeCatalog,
    localePreferences: LocalePreferences,
) : ViewModel() {

    /** نام‌های ChargeList: انگلیسی (`en`) در زبان انگلیسی، فارسی (`fn`) در بقیه. */
    private val english = localePreferences.getLanguage() == AppLanguage.English

    private val catalogOperators = chargeCatalog.operators(ChargeKind.VOUCHER)
    private val useCatalog = catalogOperators.isNotEmpty()

    private val _uiState = MutableStateFlow(
        VoucherUiState(
            operators = if (useCatalog) {
                catalogOperators.map {
                    ChargeOperatorOption(it.providerId, it.displayName(english), sadadOperatorLogo(it.providerId))
                }
            } else {
                MobileOperator.entries.map {
                    ChargeOperatorOption(it.operatorCode(), it.displayName, it.logoRes)
                }
            },
            presetAmounts = if (useCatalog) emptyList() else listOf(200_000, 500_000, 1_000_000),
        ),
    )
    val uiState: StateFlow<VoucherUiState> = _uiState.asStateFlow()

    fun onOperatorSelected(operator: ChargeOperatorOption) {
        val amounts = if (useCatalog) {
            chargeCatalog.products(ChargeKind.VOUCHER, operator.id)
                .mapNotNull { it.amountRials?.toInt() }
        } else {
            listOf(200_000, 500_000, 1_000_000)
        }
        _uiState.update {
            it.copy(
                selectedOperatorId = operator.id,
                presetAmounts = amounts,
                selectedAmount = null,
                amountText = "",
                operatorError = null,
            )
        }
    }

    fun onPresetAmountSelected(amount: Int) {
        _uiState.update {
            it.copy(
                selectedAmount = amount,
                amountText = amount.toString(),
                amountError = null,
            )
        }
    }

    fun validateAndProceed(onSuccess: (operatorId: String, productId: String, amount: Int) -> Unit) {
        val state = _uiState.value
        var operatorError: String? = null
        var amountError: String? = null
        if (state.selectedOperatorId == null) {
            operatorError = appStrings.validationSelectOperator()
        }
        if (state.selectedAmount == null || state.selectedAmount <= 0) {
            amountError = appStrings.validationEnterAmount()
        }
        if (operatorError != null || amountError != null) {
            _uiState.update {
                it.copy(operatorError = operatorError, amountError = amountError)
            }
            return
        }
        val operatorId = state.selectedOperatorId!!
        val amount = state.selectedAmount!!
        val productId = chargeCatalog.products(ChargeKind.VOUCHER, operatorId)
            .firstOrNull { it.amountRials?.toInt() == amount }
            ?.id
            .orEmpty()
        onSuccess(operatorId, productId, amount)
    }
}
