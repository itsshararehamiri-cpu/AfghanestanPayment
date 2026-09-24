package com.danesh.sadad.init

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class SadadInitProfile(
    val englishMerchantAddress: String = "",
    val linesCount: String = "",
    val headlineNo: String = "",
    val taxMemoryUniqueCode: String = "",
    val salesFundDeviceSerial: String = "",
    val salesFundMemorySerial: String = "",
)

/** مشخصات Field 63 INIT که فقط در فلیور سداد معنا دارند. */
@Singleton
class SadadInitProfileStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): SadadInitProfile = SadadInitProfile(
        englishMerchantAddress = prefs.getString(KEY_ENGLISH_ADDRESS, "").orEmpty(),
        linesCount = prefs.getString(KEY_LINES_COUNT, "").orEmpty(),
        headlineNo = prefs.getString(KEY_HEADLINE_NO, "").orEmpty(),
        taxMemoryUniqueCode = prefs.getString(KEY_TAX_MEMORY, "").orEmpty(),
        salesFundDeviceSerial = prefs.getString(KEY_DEVICE_SERIAL, "").orEmpty(),
        salesFundMemorySerial = prefs.getString(KEY_MEMORY_SERIAL, "").orEmpty(),
    )

    fun save(profile: SadadInitProfile) {
        prefs.edit()
            .putString(KEY_ENGLISH_ADDRESS, profile.englishMerchantAddress)
            .putString(KEY_LINES_COUNT, profile.linesCount)
            .putString(KEY_HEADLINE_NO, profile.headlineNo)
            .putString(KEY_TAX_MEMORY, profile.taxMemoryUniqueCode)
            .putString(KEY_DEVICE_SERIAL, profile.salesFundDeviceSerial)
            .putString(KEY_MEMORY_SERIAL, profile.salesFundMemorySerial)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "sadad_init_profile"
        const val KEY_ENGLISH_ADDRESS = "acq_address_en"
        const val KEY_LINES_COUNT = "lines_count"
        const val KEY_HEADLINE_NO = "headline_no"
        const val KEY_TAX_MEMORY = "tax_memory_unique_code"
        const val KEY_DEVICE_SERIAL = "sales_fund_device_serial"
        const val KEY_MEMORY_SERIAL = "sales_fund_memory_serial"
    }
}
