package com.danesh.core

/** نوع کلیدی که روی PED نوشته/بارگذاری می‌شود. */
enum class DeviceKeyType {
    MASTER,
    MAC,
    PIN,
    DATA,
}

/** علت شکست نوشتن/بارگذاری کلید — برای پیام مناسب در لایهٔ بالاتر. */
enum class KeyLoadError {
    /** PED/سرویس دستگاه در دسترس نیست. */
    DEVICE_UNAVAILABLE,

    /** طول کلید یا داده رمزشده نامعتبر است. */
    INVALID_KEY,

    /** کلید اصلی (TMK) برای این اندیس بارگذاری نشده است. */
    MASTER_KEY_MISSING,

    /** PED عملیات را رد کرد. */
    REJECTED_BY_DEVICE,

    /** KCV کلید روی PED با کلید ورودی یکی نیست. */
    KCV_MISMATCH,

    UNKNOWN,
}

/** نتیجهٔ نوشتن/بارگذاری یک کلید روی PED. */
sealed interface KeyLoadResult {
    val keyType: DeviceKeyType
    val index: Int

    data class Success(
        override val keyType: DeviceKeyType,
        override val index: Int,
    ) : KeyLoadResult

    data class Failure(
        override val keyType: DeviceKeyType,
        override val index: Int,
        val error: KeyLoadError,
        val message: String,
        val cause: Throwable? = null,
    ) : KeyLoadResult

    val isSuccess: Boolean get() = this is Success
}

class KeyLoadException(val failure: KeyLoadResult.Failure) :
    IllegalStateException("${failure.keyType} key load failed at index=${failure.index}: ${failure.message}", failure.cause)

/** برای کدهایی که روی exception تکیه دارند: شکست را به [KeyLoadException] تبدیل می‌کند. */
fun KeyLoadResult.getOrThrow(): KeyLoadResult.Success = when (this) {
    is KeyLoadResult.Success -> this
    is KeyLoadResult.Failure -> throw KeyLoadException(this)
}
