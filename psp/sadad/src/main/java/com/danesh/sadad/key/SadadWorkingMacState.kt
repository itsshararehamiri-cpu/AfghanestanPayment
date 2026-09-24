package com.danesh.sadad.key

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * بعد از اولین LOGON موفق با CHANGE_KEY، MAC کاری روی PED است و لاگان‌های بعدی
 * باید با همان working MAC ساخته شوند — نه INIT MAC.
 *
 * اسلات کارت C: TMK / Init MAC / PIN / DATA قبل از لاگان.
 * اسلات کاری (C+1): PIN/MAC/DATA فیلد ۴۸ بعد از اولین لاگان.
 */
@Singleton
class SadadWorkingMacState private constructor(
    private val prefs: SharedPreferences?,
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
    )

    @Volatile
    private var cached: Boolean = prefs?.getBoolean(KEY_WORKING_MAC, false) ?: false

    @Volatile
    private var cachedKeyIndex: Int = prefs?.getInt(KEY_PED_INDEX, DEFAULT_KEY_INDEX) ?: DEFAULT_KEY_INDEX

    @Volatile
    private var cachedWorkingKeyIndex: Int =
        prefs?.getInt(KEY_WORKING_INDEX, cachedKeyIndex + 1) ?: (cachedKeyIndex + 1)

    @Volatile
    private var cachedRsaKeyIndex: Int? =
        prefs?.takeIf { it.contains(KEY_RSA_INDEX) }?.getInt(KEY_RSA_INDEX, 0)

    @Volatile
    private var hasSavedCardCIndex: Boolean = prefs?.contains(KEY_PED_INDEX) == true

    fun hasWorkingMac(): Boolean = cached

    fun keyIndex(): Int = cachedKeyIndex

    /** اسلات Init MAC / TMK قبل از لاگان — همان اندیس کارت C. */
    fun initMacIndex(): Int = keyIndex()

    /** اسلات کلید کاری بعد از لاگان — اندیس کارت C به‌علاوه یک. */
    fun workingKeyIndex(): Int = cachedWorkingKeyIndex

    fun rsaKeyIndex(): Int? = cachedRsaKeyIndex

    fun persistedCardCIndex(): Int? = if (hasSavedCardCIndex) cachedKeyIndex else null

    fun masterKeyIndexForDe59(): String = keyIndex().toString().padStart(3, '0')

    fun saveRsaKeyIndex(index: Int) {
        cachedRsaKeyIndex = index
        prefs?.edit()?.putInt(KEY_RSA_INDEX, index)?.apply()
    }

    fun saveKeyIndex(index: Int) {
        saveKeyIndices(cardCIndex = index, rsaKeyIndex = cachedRsaKeyIndex)
    }

    fun saveKeyIndices(cardCIndex: Int, rsaKeyIndex: Int? = cachedRsaKeyIndex) {
        cachedKeyIndex = cardCIndex
        cachedWorkingKeyIndex = cardCIndex + 1
        hasSavedCardCIndex = true
        rsaKeyIndex?.let { cachedRsaKeyIndex = it }
        prefs?.edit()?.apply {
            putInt(KEY_PED_INDEX, cardCIndex)
            putInt(KEY_WORKING_INDEX, cardCIndex + 1)
            rsaKeyIndex?.let { putInt(KEY_RSA_INDEX, it) }
            apply()
        }
    }

    fun markWorkingMacLoaded() {
        cached = true
        prefs?.edit()?.putBoolean(KEY_WORKING_MAC, true)?.apply()
    }

    fun clearWorkingMac() {
        cached = false
        prefs?.edit()?.putBoolean(KEY_WORKING_MAC, false)?.apply()
    }

    companion object {
        fun inMemoryForTests(): SadadWorkingMacState = SadadWorkingMacState(prefs = null)

        const val DEFAULT_KEY_INDEX = 16
        private const val PREFS_NAME = "sadad_mac_key_state"
        private const val KEY_WORKING_MAC = "working_mac_loaded"
        private const val KEY_PED_INDEX = "ped_key_index"
        private const val KEY_WORKING_INDEX = "ped_working_key_index"
        private const val KEY_RSA_INDEX = "rsa_key_index"
    }
}
