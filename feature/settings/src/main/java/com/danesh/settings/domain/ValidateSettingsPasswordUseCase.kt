package com.danesh.settings.domain

import com.danesh.settings.data.SettingsPasswordRepository
import com.danesh.settings.model.AppRole
import javax.inject.Inject

class ValidateSettingsPasswordUseCase @Inject constructor(
    private val repository: SettingsPasswordRepository,
) {
    operator fun invoke(role: AppRole, password: String): Boolean = when (role) {
        AppRole.Support -> repository.validateSupportPassword(password)
        AppRole.Merchant -> repository.validateMerchantPassword(password)
    }

    fun mustChangeMerchantPassword(): Boolean = repository.mustChangeMerchantPassword()

    fun requiresMerchantPasswordChange(): Boolean = repository.requiresMerchantPasswordChange()
}
