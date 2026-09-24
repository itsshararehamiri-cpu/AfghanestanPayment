package com.danesh.report.data

import com.danesh.settings.data.SettingsPasswordRepository
import com.danesh.settings.model.MerchantPasswordCheck
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportPasswordRepository @Inject constructor(
    private val settingsPasswordRepository: SettingsPasswordRepository,
) {
    fun validate(password: String): Boolean =
        check(password) == MerchantPasswordCheck.VALID

    fun check(password: String): MerchantPasswordCheck =
        settingsPasswordRepository.checkMerchantPassword(password)

    fun mustChangeMerchantPassword(): Boolean =
        settingsPasswordRepository.mustChangeMerchantPassword()

    fun requiresMerchantPasswordChange(): Boolean =
        settingsPasswordRepository.requiresMerchantPasswordChange()
}
