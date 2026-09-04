package com.danesh.api

/**
 * سیاست پیکربندی اولیه هر PSP.
 *
 * - BP: init/logon ISO با بلیط
 * - HP: inject مستقیم کلیدهای از پیش تعریف‌شده روی PED
 */
interface InitialConfigurationPolicy {
    val requiresBallotTickets: Boolean

    /** راه‌اندازی ترمینال از طریق FirstInit/Logon — BP */
    val usesTerminalSetupLogon: Boolean
        get() = false

    /** inject کلیدهای کاری روی PED — فقط برای PSPهایی که [requiresBallotTickets]=false */
    suspend fun injectKeys(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Direct key injection is not supported"),
    )
}
