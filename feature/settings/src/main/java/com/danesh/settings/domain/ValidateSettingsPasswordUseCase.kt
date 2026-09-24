package com.danesh.settings.domain

import com.danesh.settings.data.SettingsPasswordRepository
import com.danesh.settings.model.AppRole
import com.danesh.settings.model.MerchantPasswordCheck
import javax.inject.Inject

class ValidateSettingsPasswordUseCase @Inject constructor(
    private val repository: SettingsPasswordRepository,
) {
    operator fun invoke(role: AppRole, password: String): Boolean =
        check(role, password) == MerchantPasswordCheck.VALID

    fun check(role: AppRole, password: String): MerchantPasswordCheck = when (role) {
        AppRole.Support -> if (repository.validateSupportPassword(password)) {
            MerchantPasswordCheck.VALID
        } else {
            MerchantPasswordCheck.WRONG
        }
        AppRole.Merchant -> repository.checkMerchantPassword(password)
    }

    fun mustChangeMerchantPassword(): Boolean = repository.mustChangeMerchantPassword()

    fun requiresMerchantPasswordChange(): Boolean = repository.requiresMerchantPasswordChange()
}
