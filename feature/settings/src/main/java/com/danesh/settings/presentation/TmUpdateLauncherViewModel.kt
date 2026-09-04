package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import com.danesh.api.SupportCatalog
import com.danesh.common.connection.ConnectionPreferences
import com.danesh.settings.domain.MerchantSupportServiceResolver
import com.danesh.settings.model.MerchantSupportLaunchRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TmUpdateLauncherViewModel @Inject constructor(
    private val connectionPreferences: ConnectionPreferences,
    private val supportCatalog: SupportCatalog,
    private val merchantSupportServiceResolver: MerchantSupportServiceResolver,
) : ViewModel() {

    fun resolveTmUpdateRequest(): TmUpdateLaunchResult {
        if (!connectionPreferences.isTmsServerConfigured()) {
            return TmUpdateLaunchResult.ServerNotConfigured
        }
        val service = merchantSupportServiceResolver.findTmUpdate(supportCatalog)
            ?: return TmUpdateLaunchResult.ServiceUnavailable
        return TmUpdateLaunchResult.Ready(
            MerchantSupportLaunchRequest(
                amount = service.amount,
                serviceId = service.serviceId,
                title = service.title,
            ),
        )
    }
}

sealed interface TmUpdateLaunchResult {
    data object ServerNotConfigured : TmUpdateLaunchResult
    data object ServiceUnavailable : TmUpdateLaunchResult
    data class Ready(val request: MerchantSupportLaunchRequest) : TmUpdateLaunchResult
}
