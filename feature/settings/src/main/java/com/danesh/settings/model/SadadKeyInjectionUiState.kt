package com.danesh.settings.model

import com.danesh.settings.domain.StartupStepResult

enum class SadadKeyInjectionStep {
    /** «لطفاً کارت کلید را وارد کنید» — در انتظار دریافت کارت ICC. */
    WAIT_CARD,

    /** ایندکس و رمز کارت‌های A و C. */
    FORM,
    PROCESSING,

    /** کارت A و C دو کارت جدا هستند: خارج کردن A و وارد کردن C. */
    SWAP_CARD,

    /** کلیدگذاری موفق — «لطفاً کارت را خارج کنید». */
    SUCCESS,
    ERROR,
}

data class SadadKeyInjectionUiState(
    val step: SadadKeyInjectionStep = SadadKeyInjectionStep.WAIT_CARD,
    val cardAIndex: String = "",
    val cardCIndex: String = "",
    val cardAPin: String = "",
    val cardCPin: String = "",
    val cardAIndexError: String? = null,
    val cardCIndexError: String? = null,
    val cardAPinError: String? = null,
    val cardCPinError: String? = null,
    /** پیام وضعیت مرحله جاری (در انتظار کارت، مرحله پردازش، ...). */
    val statusMessage: String? = null,
    /** انتظار برای کارت به پایان رسید یا کارت‌خوان در دسترس نیست — دکمه «تلاش مجدد». */
    val waitFailedMessage: String? = null,
    val swapCardRemoved: Boolean = false,
    val errorMessage: String? = null,
    val kcv: KeyLoadingKcvSummary? = null,
    val notes: List<String> = emptyList(),
    val initResult: StartupStepResult? = null,
    val logonResult: StartupStepResult? = null,
    val printErrorMessage: String? = null,
    val cardRemoved: Boolean = false,
)
