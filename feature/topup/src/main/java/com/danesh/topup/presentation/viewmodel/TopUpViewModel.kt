package com.danesh.topup.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.danesh.api.ChargeCatalog
import com.danesh.api.ChargeKind
import com.danesh.api.ChargeProduct
import com.danesh.common.strings.AppStrings
import com.danesh.topup.ChargeOperatorOption
import com.danesh.topup.MobileOperator
import com.danesh.topup.operatorCode
import com.danesh.topup.sadadOperatorLogo
import com.danesh.topup.ui.formatMobileInput
import com.danesh.topup.ui.mobileNumberForSubmit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TopUpUiState(
    val mobileNumber: String = "",
    val operators: List<ChargeOperatorOption> = emptyList(),
    val selectedOperatorId: String? = null,
    val groups: List<String> = emptyList(),
    val selectedGroup: String? = null,
    val presetAmounts: List<Int> = emptyList(),
    val selectedAmount: Int? = null,
    val amountText: String = "",
    val variableAmount: Boolean = false,
    val minChargeAmount: Int? = null,
    val mobileError: String? = null,
    val operatorError: String? = null,
    val amountError: String? = null,
)

@HiltViewModel
class TopUpViewModel @Inject constructor(
    private val appStrings: AppStrings,
    private val chargeCatalog: ChargeCatalog,
) : ViewModel() {

    private val catalogOperators = chargeCatalog.operators(ChargeKind.TOPUP)
    private val useCatalog = catalogOperators.isNotEmpty()

    private val _uiState = MutableStateFlow(
        TopUpUiState(
            operators = if (useCatalog) {
                catalogOperators.map {
                    ChargeOperatorOption(it.providerId, it.nameFa, sadadOperatorLogo(it.providerId))
                }
            } else {
                MobileOperator.entries.map {
                    ChargeOperatorOption(it.operatorCode(), it.displayName, it.logoRes)
                }
            },
            presetAmounts = if (useCatalog) emptyList() else listOf(5_000, 10_000, 20_000, 50_000),
        ),
    )
    val uiState: StateFlow<TopUpUiState> = _uiState.asStateFlow()

    fun onMobileChange(value: String) {
        _uiState.update { it.copy(mobileNumber = formatMobileInput(value), mobileError = null) }
    }

    fun onOperatorSelected(operator: ChargeOperatorOption) {
        val products = chargeCatalog.products(ChargeKind.TOPUP, operator.id)
        val groups = products.map { it.groupKey() }.distinct().filter { it.isNotBlank() }
        val selectedGroup = groups.singleOrNull()
        val minAmount = chargeCatalog.operators(ChargeKind.TOPUP)
            .firstOrNull { it.providerId == operator.id }
            ?.minChargeAmount?.toInt()
        _uiState.update {
            it.copy(
                selectedOperatorId = operator.id,
                groups = groups,
                selectedGroup = selectedGroup,
                operatorError = null,
                selectedAmount = null,
                amountText = "",
                minChargeAmount = minAmount,
            ).withProducts(productsFor(operator.id, selectedGroup))
        }
    }

    fun onGroupSelected(group: String) {
        val operatorId = _uiState.value.selectedOperatorId ?: return
        _uiState.update {
            it.copy(
                selectedGroup = group,
                selectedAmount = null,
                amountText = "",
            ).withProducts(productsFor(operatorId, group))
        }
    }

    fun onAmountChange(input: String) {
        val digits = input.filter { it.isDigit() }
        val amount = digits.toIntOrNull() ?: 0
        _uiState.update {
            it.copy(selectedAmount = amount, amountText = digits, amountError = null)
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

    fun validateAndProceed(
        onSuccess: (mobile: String, operatorId: String, productId: String, amount: Int) -> Unit,
    ) {
        val state = _uiState.value
        val normalizedMobile = mobileNumberForSubmit(state.mobileNumber)

        var mobileError: String? = null
        var operatorError: String? = null
        var amountError: String? = null

        if (normalizedMobile.isEmpty()) {
            mobileError = appStrings.validationEnterMobile()
        } else if (normalizedMobile.length != 11) {
            mobileError = appStrings.validationMobile10Digits()
        } else if (!normalizedMobile.startsWith("09")) {
            mobileError = appStrings.validationMobilePrefix()
        }

        if (state.selectedOperatorId == null) {
            operatorError = appStrings.validationSelectOperator()
        }
        if (state.groups.size > 1 && state.selectedGroup == null) {
            operatorError = appStrings.validationSelectOperator()
        }

        val min = state.minChargeAmount
        if (state.selectedAmount == null || state.selectedAmount <= 0) {
            amountError = appStrings.validationEnterAmount()
        } else if (min != null && state.selectedAmount < min) {
            amountError = appStrings.validationEnterAmount()
        }

        if (mobileError != null || operatorError != null || amountError != null) {
            _uiState.update {
                it.copy(
                    mobileError = mobileError,
                    operatorError = operatorError,
                    amountError = amountError,
                )
            }
            return
        }

        val operatorId = state.selectedOperatorId!!
        val amount = state.selectedAmount!!
        val productId = productsFor(operatorId, state.selectedGroup)
            .firstOrNull { product ->
                product.amountRials?.toInt() == amount || product.amountRials == null
            }
            ?.id
            .orEmpty()
        onSuccess(normalizedMobile, operatorId, productId, amount)
    }

    private fun productsFor(operatorId: String, group: String?): List<ChargeProduct> {
        val products = chargeCatalog.products(ChargeKind.TOPUP, operatorId)
        if (group.isNullOrBlank()) return products
        return products.filter { it.groupKey() == group }
    }

    private fun TopUpUiState.withProducts(products: List<ChargeProduct>): TopUpUiState {
        val variable = products.isNotEmpty() && products.all { it.amountRials == null }
        val amounts = products.mapNotNull { it.amountRials?.toInt() }
        return copy(
            variableAmount = variable || !useCatalog,
            presetAmounts = if (useCatalog) amounts else listOf(5_000, 10_000, 20_000, 50_000),
        )
    }

    private fun ChargeProduct.groupKey(): String {
        if (groupLabelFa.isNotBlank()) return groupLabelFa
        if (amountRials == null) return labelFa
        return ""
    }
}
