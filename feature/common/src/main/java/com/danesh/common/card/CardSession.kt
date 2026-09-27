package com.danesh.common.card

import com.danesh.core.emv.ContactlessCardData
import javax.inject.Inject
import javax.inject.Singleton

/** روش ورود کارت: کشیدن کارت مغناطیسی یا نزدیک کردن کارت کهربا (NFC/EMV). */
enum class CardEntryMode {
    MAGNETIC,
    KAHROBA,
}

/** داده‌ی کارت کهربا برای ارسال تراکنش پس از صفحه‌ی PIN. */
data class KahrobaCardSession(
    val card: ContactlessCardData,
    /** PIN لازم است (کارت خواسته، مبلغ بالای سقف، مانده، یا سوئیچ کد 76 داده). */
    val pinRequired: Boolean,
)

@Singleton
class CardSession @Inject constructor() {
    var track2: String = ""
        private set

    var pan: String = ""
        private set

    /** فقط وقتی کارت با کهربا خوانده شده باشد مقدار دارد. */
    var kahroba: KahrobaCardSession? = null
        private set

    val entryMode: CardEntryMode
        get() = if (kahroba != null) CardEntryMode.KAHROBA else CardEntryMode.MAGNETIC

    /** iccData (فیلد ۵۵) کارت کهربا؛ برای کارت مغناطیسی خالی است. */
    val iccData: String
        get() = kahroba?.card?.iccData.orEmpty()

    fun set(track2: String, pan: String) {
        // اگر همان کارت کهربا دوباره ثبت شود (مثلاً از صفحه PIN) داده‌ی EMV حفظ می‌شود.
        if (kahroba != null && kahroba?.card?.track2 != track2) {
            kahroba = null
        }
        this.track2 = track2
        this.pan = pan
    }

    fun setKahroba(card: ContactlessCardData, pinRequired: Boolean) {
        track2 = card.track2
        pan = card.pan
        kahroba = KahrobaCardSession(card = card, pinRequired = pinRequired)
    }

    fun markKahrobaPinRequired() {
        kahroba = kahroba?.copy(pinRequired = true)
    }

    fun clear() {
        track2 = ""
        pan = ""
        kahroba = null
    }
}
