package com.danesh.common.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * رمز منوی پشتیبان که سوئیچ (مثلاً در LOGON) تعیین می‌کند.
 *
 * وقتی رمزی ذخیره نشده باشد (`null`)، رمز پیش‌فرض برنامه (معکوس ساعت) معتبر است.
 * این کلاس به PSP خاصی وابسته نیست؛ PSP فقط مقدار را تنظیم/پاک می‌کند.
 */
@Singleton
class SupportPasswordStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** رمز تعیین‌شده توسط سوئیچ، یا `null` یعنی رمز پیش‌فرض. */
    fun hostPassword(): String? = prefs.getString(KEY_PASSWORD, null)?.takeIf { it.isNotEmpty() }

    fun setHostPassword(password: String) {
        prefs.edit().putString(KEY_PASSWORD, password).apply()
    }

    /** بازگشت به رمز پیش‌فرض (معکوس ساعت). */
    fun resetToDefault() {
        prefs.edit().remove(KEY_PASSWORD).apply()
    }

    private companion object {
        const val PREFS_NAME = "support_password"
        const val KEY_PASSWORD = "host_password"
    }
}
