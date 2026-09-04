package com.danesh.report.data

import com.danesh.settings.data.SettingsPasswordRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportPasswordRepository @Inject constructor(
    private val settingsPasswordRepository: SettingsPasswordRepository,
) {
    fun validate(password: String): Boolean =
        settingsPasswordRepository.validateMerchantPassword(password)

    fun mustChangeMerchantPassword(): Boolean =
        settingsPasswordRepository.mustChangeMerchantPassword()

    fun requiresMerchantPasswordChange(): Boolean =
        settingsPasswordRepository.requiresMerchantPasswordChange()
}
