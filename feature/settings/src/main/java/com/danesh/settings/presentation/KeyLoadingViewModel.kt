package com.danesh.settings.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.BallotType
import com.danesh.api.InitInput
import com.danesh.api.InitialConfigurationPolicy
import com.danesh.api.PspGateway
import com.danesh.core.Device
import com.danesh.core.KCV
import com.danesh.settings.R
import com.danesh.settings.util.SettingsTextInputFilters
import com.danesh.settings.model.KeyLoadingKcvSummary
import com.danesh.settings.model.KeyLoadingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BpKeyLoading"

@HiltViewModel
class KeyLoadingViewModel @Inject constructor(
    private val pspGateway: PspGateway,
    private val initialConfigurationPolicy: InitialConfigurationPolicy,
    private val device: Device,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        KeyLoadingUiState(
            requiresBallotTickets = initialConfigurationPolicy.requiresBallotTickets,
        ),
    )
    val uiState: StateFlow<KeyLoadingUiState> = _uiState.asStateFlow()

    /** true برای PSPهایی که کلیدگذاری با کارت هوشمند دارند (سداد) — UI باید جریان دیگری نمایش دهد. */
    val usesKeyCardLoading: Boolean = initialConfigurationPolicy.usesKeyCardLoading

    fun updateFirstBallotTicket(ticket: String) {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                firstBallotTicket = SettingsTextInputFilters.ballotTicketDigitsOnly(ticket),
                resultMessage = null,
                kcvSummary = null,
                isSuccess = false,
            )
        }
    }

    fun updateSecondBallotTicket(ticket: String) {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                secondBallotTicket = SettingsTextInputFilters.ballotTicketDigitsOnly(ticket),
                resultMessage = null,
                kcvSummary = null,
                isSuccess = false,
            )
        }
    }

    fun scanFirstBallotTicket() = scanTicket(::updateFirstBallotTicket)

    fun scanSecondBallotTicket() = scanTicket(::updateSecondBallotTicket)

    private fun scanTicket(onTicket: (String) -> Unit) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            device.scan(
                context = appContext,
                onSuccess = onTicket,
                onError = { message ->
                    _uiState.update { it.copy(resultMessage = message) }
                },
                onTimeout = {
                    _uiState.update {
                        it.copy(
                            resultMessage = appContext.getString(
                                R.string.settings_initial_configuration_qr_scan_timeout,
                            ),
                        )
                    }
                },
                onCancel = {},
            )
        }
    }

    fun confirm() {
        if (_uiState.value.isLoading) return
        if (_uiState.value.requiresBallotTickets) {
            runInitSequence()
        } else {
            runLogon()
        }
    }

    private fun runLogon() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, resultMessage = null, kcvSummary = null, isSuccess = false) }
            Log.d("TAG", "logon: dddddddddddddddddddddlenin2")

            val result = runCatching { pspGateway.logon(masterKey = "") }.getOrElse { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resultMessage = throwable.message.orEmpty().ifBlank {
                            appContext.getString(R.string.settings_initial_configuration_error_generic)
                        },
                    )
                }
                return@launch
            }

            if (result.isSuccess) {
                val kcvSummary = buildKcvSummary()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        kcvSummary = kcvSummary,
                        resultMessage = appContext.getString(R.string.settings_support_operation_success),
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resultMessage = result.responseMessage.ifBlank {
                            appContext.getString(R.string.settings_initial_configuration_error_failed)
                        },
                    )
                }
            }
        }
    }

    private fun runInitSequence() {
        val firstTicket = _uiState.value.firstBallotTicket.trim()
        val secondTicket = _uiState.value.secondBallotTicket.trim()
        when {
            firstTicket.isBlank() -> {
                _uiState.update {
                    it.copy(
                        resultMessage = appContext.getString(
                            R.string.settings_key_loading_first_ballot_required,
                        ),
                    )
                }
                return
            }
            secondTicket.isBlank() -> {
                _uiState.update {
                    it.copy(
                        resultMessage = appContext.getString(
                            R.string.settings_key_loading_second_ballot_required,
                        ),
                    )
                }
                return
            }
        }

        viewModelScope.launch {
            // بلیط‌ها نباید پس از شروع Init در UI/State بمانند (مستند به‌پرداخت).
            _uiState.update {
                it.copy(
                    isLoading = true,
                    resultMessage = null,
                    kcvSummary = null,
                    isSuccess = false,
                    firstBallotTicket = "",
                    secondBallotTicket = "",
                )
            }

            val secondResult = runCatching {
                pspGateway.init(
                    InitInput(
                        ballotType = BallotType.SECOND,
                        firstBallotTicket = firstTicket,
                        secondBallotTicket = secondTicket,
                    ),
                )
            }.getOrElse { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        firstBallotTicket = "",
                        secondBallotTicket = "",
                        resultMessage = appContext.getString(
                            R.string.settings_key_loading_error_with_message,
                            throwable.message.orEmpty().ifBlank {
                                appContext.getString(
                                    R.string.settings_initial_configuration_error_generic,
                                )
                            },
                        ),
                    )
                }
                return@launch
            }

            if (!secondResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        firstBallotTicket = "",
                        secondBallotTicket = "",
                        resultMessage = appContext.getString(
                            R.string.settings_key_loading_error_response,
                            secondResult.responseMessage.ifBlank {
                                appContext.getString(
                                    R.string.settings_initial_configuration_error_failed,
                                )
                            },
                        ),
                    )
                }
                return@launch
            }

            val kcvSummary = buildKcvSummary(masterOnly = true)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = true,
                    kcvSummary = kcvSummary,
                    firstBallotTicket = "",
                    secondBallotTicket = "",
                    resultMessage = appContext.getString(R.string.settings_key_loading_success),
                )
            }
        }
    }

    private suspend fun buildKcvSummary(masterOnly: Boolean = false): KeyLoadingKcvSummary {
        val kcv = runCatching { device.getKCv() }.getOrElse { KCV("", "", "", "") }
        return KeyLoadingKcvSummary(
            master = kcv.master.ifBlank { "-" },
            mac = if (masterOnly) "-" else kcv.mac.ifBlank { "-" },
            pin = if (masterOnly) "-" else kcv.pin.ifBlank { "-" },
            data = if (masterOnly) "-" else kcv.data.ifBlank { "-" },
            masterOnly = masterOnly,
        )
    }
}
