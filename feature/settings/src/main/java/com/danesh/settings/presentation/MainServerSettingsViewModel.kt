package com.danesh.settings.presentation



import androidx.lifecycle.ViewModel

import com.danesh.common.connection.ConnectionAddressValidator

import com.danesh.common.connection.ConnectionChannel

import com.danesh.common.connection.ConnectionPreferences

import com.danesh.settings.model.MainServerSettingsUiState

import com.danesh.settings.util.SettingsTextInputFilter

import com.danesh.settings.util.SettingsTextInputFilters

import dagger.hilt.android.lifecycle.HiltViewModel

import javax.inject.Inject

import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.flow.update



@HiltViewModel

class MainServerSettingsViewModel @Inject constructor(

    private val connectionPreferences: ConnectionPreferences,

) : ViewModel() {



    private val _uiState = MutableStateFlow(loadState())

    val uiState: StateFlow<MainServerSettingsUiState> = _uiState.asStateFlow()



    fun setConnectionType(type: ConnectionChannel) {

        _uiState.update {

            it.copy(

                connectionType = type,

                serverIp = readServerIp(type),

                serverPort = readServerPort(type),

            )

        }

    }



    fun setServerIp(value: String) {

        _uiState.update {

            it.copy(

                serverIp = SettingsTextInputFilters.apply(SettingsTextInputFilter.IpAddress, value),

                serverIpError = null,

                validationSummary = null,

            )

        }

    }



    fun setServerPort(value: String) {

        _uiState.update {

            it.copy(

                serverPort = SettingsTextInputFilters.apply(SettingsTextInputFilter.DigitsOnly, value),

                serverPortError = null,

                validationSummary = null,

            )

        }

    }



    fun setTmsIp(value: String) {

        _uiState.update {

            it.copy(

                tmsIp = SettingsTextInputFilters.apply(SettingsTextInputFilter.IpAddress, value),

                tmsIpError = null,

                validationSummary = null,

            )

        }

    }



    fun setTmsPort(value: String) {

        _uiState.update {

            it.copy(

                tmsPort = SettingsTextInputFilters.apply(SettingsTextInputFilter.DigitsOnly, value),

                tmsPortError = null,

                validationSummary = null,

            )

        }

    }



    fun setIdleTimeMinutes(value: String) {

        _uiState.update {

            it.copy(

                idleTimeMinutes = SettingsTextInputFilters.apply(SettingsTextInputFilter.PositiveInteger, value),

                idleTimeError = null,

                validationSummary = null,

            )

        }

    }



    fun save(

        ipEmptyError: String,

        ipInvalidError: String,

        portEmptyError: String,

        portInvalidError: String,

        idleInvalidError: String,

    ) {

        val state = _uiState.value

        val serverIpError = when (ConnectionAddressValidator.ipError(state.serverIp)) {

            ConnectionAddressValidator.IpValidationError.EMPTY -> ipEmptyError

            ConnectionAddressValidator.IpValidationError.INVALID -> ipInvalidError

            null -> null

        }

        val serverPortError = when (ConnectionAddressValidator.portError(state.serverPort)) {

            ConnectionAddressValidator.PortValidationError.EMPTY -> portEmptyError

            ConnectionAddressValidator.PortValidationError.INVALID -> portInvalidError

            null -> null

        }

        val tmsIpError = when (ConnectionAddressValidator.ipError(state.tmsIp)) {

            ConnectionAddressValidator.IpValidationError.EMPTY -> ipEmptyError

            ConnectionAddressValidator.IpValidationError.INVALID -> ipInvalidError

            null -> null

        }

        val tmsPortError = when (ConnectionAddressValidator.portError(state.tmsPort)) {

            ConnectionAddressValidator.PortValidationError.EMPTY -> portEmptyError

            ConnectionAddressValidator.PortValidationError.INVALID -> portInvalidError

            null -> null

        }

        val idleTimeError = when {

            state.idleTimeMinutes.isBlank() -> idleInvalidError

            !SettingsTextInputFilters.isValidPositiveInteger(state.idleTimeMinutes) -> idleInvalidError

            else -> null

        }



        if (serverIpError != null || serverPortError != null ||

            tmsIpError != null || tmsPortError != null || idleTimeError != null

        ) {

            val summary = listOfNotNull(

                serverIpError,

                serverPortError,

                tmsIpError,

                tmsPortError,

                idleTimeError,

            ).firstOrNull()

            _uiState.update {

                it.copy(

                    serverIpError = serverIpError,

                    serverPortError = serverPortError,

                    tmsIpError = tmsIpError,

                    tmsPortError = tmsPortError,

                    idleTimeError = idleTimeError,

                    validationSummary = summary,

                    savedSuccessfully = false,

                    navigateToWifiSelection = false,

                )

            }

            return

        }



        val port = state.serverPort.toInt()

        val tmsPort = state.tmsPort.toInt()

        val idleMinutes = state.idleTimeMinutes.toInt()



        connectionPreferences.setMainServerConnectionType(state.connectionType)

        when (state.connectionType) {

            ConnectionChannel.WIFI -> {

                connectionPreferences.saveWifiIp(state.serverIp)

                connectionPreferences.saveWifiPort(port)

            }

            ConnectionChannel.GPRS -> {

                connectionPreferences.saveGprsIp(state.serverIp)

                connectionPreferences.saveGprsPort(port)

            }

        }

        connectionPreferences.saveTMSIp(state.tmsIp)

        connectionPreferences.saveTMSPort(tmsPort)

        connectionPreferences.setIdleTimeMinutes(idleMinutes)



        if (state.connectionType == ConnectionChannel.WIFI) {

            _uiState.update {

                it.copy(

                    validationSummary = null,

                    savedSuccessfully = false,

                    navigateToWifiSelection = true,

                )

            }

        } else {

            _uiState.update {

                it.copy(

                    validationSummary = null,

                    savedSuccessfully = true,

                    navigateToWifiSelection = false,

                )

            }

        }

    }



    fun consumeWifiNavigationRequest() {

        _uiState.update { it.copy(navigateToWifiSelection = false) }

    }



    fun clearSavedFlag() {

        _uiState.update { it.copy(savedSuccessfully = false) }

    }



    private fun loadState(): MainServerSettingsUiState {

        val connectionType = connectionPreferences.getMainServerConnectionType()

        return MainServerSettingsUiState(

            connectionType = connectionType,

            serverIp = readServerIp(connectionType),

            serverPort = readServerPort(connectionType),

            tmsIp = connectionPreferences.getTMSIp(),

            tmsPort = connectionPreferences.getTMSPort().takeIf { it > 0 }?.toString().orEmpty(),

            idleTimeMinutes = connectionPreferences.getIdleTimeMinutes()

                .takeIf { it > 0 }

                ?.toString()

                .orEmpty(),

        )

    }



    private fun readServerIp(type: ConnectionChannel): String = when (type) {

        ConnectionChannel.WIFI -> connectionPreferences.getWifiIp()

        ConnectionChannel.GPRS -> connectionPreferences.getGprsIp()

    }



    private fun readServerPort(type: ConnectionChannel): String {

        val port = when (type) {

            ConnectionChannel.WIFI -> connectionPreferences.getWifiPort()

            ConnectionChannel.GPRS -> connectionPreferences.getGprsPort()

        }

        return port.takeIf { it > 0 }?.toString().orEmpty()

    }

}


