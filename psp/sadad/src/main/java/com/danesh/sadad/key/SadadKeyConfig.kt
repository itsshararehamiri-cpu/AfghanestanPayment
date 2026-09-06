package com.danesh.sadad.key

/**
 * ثابت‌های ISO مخصوص سداد — طبق سند «Terminal Message Protocol» سداد (2016، بر مبنای ISO8583/شتاب).
 *
 * نکتهٔ مهم سند: DE24 برای *همهٔ* تراکنش‌های سداد صرفاً NII ثابت "007" است — برخلاف همراه‌پی که
 * از همین بیت به‌عنوان «Function Code» تراکنش استفاده می‌کند. همچنین فیلد 48 سداد برخلاف
 * همراه‌پی/به‌پرداخت یک قالب TLV ندارد؛ هر تراکنش آرایش خام ثابت خودش را دارد (نگاه کنید به
 * سازندهٔ پیام هر تراکنش).
 */
object SadadKeyConfig {
    const val MASTER_KEY_HEX = "3132333435363738393A3B3C3D3E3F20"
    const val ENCRYPTED_PIN_KEY_HEX = "5C8A22317AD2DC5A03F674BACDB2BC37"
    const val ENCRYPTED_MAC_KEY_HEX = "F7C6D4AFD6C3832AA16C07D1BA9F62DD"

    const val DEFAULT_TERMINAL_ID = "12345678"//
    const val DEFAULT_MERCHANT_ID = "44236789"//
    const val TERMINAL_ID_LENGTH = 8
    const val MERCHANT_ID_LENGTH = 15

    const val POS_ENTRY_MODE = "210101213144"
    const val CARD_TO_CARD_POS_ENTRY_MODE = "100010100131"
    const val MERCHANT_TYPE = "6012"
    const val CARDHOLDER_BILLING_CURRENCY = "971"

    /** DE24 NII — طبق سند، برای همهٔ تراکنش‌های سداد ثابت است. */
    const val NII = "007"
    /** DE22 POS Entry Mode (n 3) طبق سند. */
    const val ISO_POS_ENTRY_MODE = "021"
    /** DE25 POS Condition Code (n 2) طبق سند. */
    const val POS_CONDITION_CODE = "14"

    /** موجودی (2-BALANCE) — MTI 0100 / DE3 310000 */
    const val BALANCE_MTI = "0100"
    const val BALANCE_PROCESSING_CODE = "310000"
    const val BALANCE_POS_ENTRY_MODE = ISO_POS_ENTRY_MODE
    const val BALANCE_NII = NII
    const val BALANCE_POS_CONDITION_CODE = POS_CONDITION_CODE

    /** خرید (3-SALE) — MTI 0200 / DE3 000000، چندپذیرندگی */
    const val PURCHASE_MTI = "0200"
    const val PURCHASE_PROCESSING_CODE = "000000"
    const val PURCHASE_POS_ENTRY_MODE = ISO_POS_ENTRY_MODE
    const val PURCHASE_NII = NII
    const val PURCHASE_POS_CONDITION_CODE = POS_CONDITION_CODE
    /** DE61 Mode 1: one terminal one merchant — Mode n2 + MID n2 */
    const val PURCHASE_DE61_MODE_ONE = "01"
    const val PURCHASE_DEFAULT_MERCHANT_SLOT = "01"

    /** شارژ/ووچر (4-CHARGE) — MTI 0200 / DE3 150000؛ DE48 = ProviderID(4)+CategoryID(2)+Space+ChargeCount(2) */
    const val VOUCHER_MTI = "0200"
    const val VOUCHER_PROCESSING_CODE = "150000"
    const val CHARGE_PROVIDER_ID_LENGTH = 4
    const val CHARGE_CATEGORY_ID_LENGTH = 2
    const val CHARGE_COUNT_LENGTH = 2
    const val CHARGE_DEFAULT_CATEGORY_ID = "01"
    const val CHARGE_DEFAULT_COUNT = "01"

    /** شارژ مستقیم (13-TOPUP) — MTI 0200 / DE3 230000. سند فیلد 48 برای این تراکنش تعریف نکرده؛
     * اپراتور/شماره موبایل با قالب مستند‌شدهٔ DE63 (Count/FunctionCode/#len/Data) حمل می‌شود. */
    const val TOPUP_MTI = "0200"
    const val TOPUP_PROCESSING_CODE = "230000"
    const val TOPUP_OPERATOR_FUNCTION_CODE = "018"
    const val TOPUP_MOBILE_FUNCTION_CODE = "019"

    /** پرداخت قبض (5-BILL_PAYMENT) — MTI 0200 / DE3 170000 */
    const val BILL_MTI = "0200"
    /** استعلام قبض (6-BILL INQUIRY) — MTI 0100 / DE3 170000 (همان Bill_ID+Payment_ID فیلد 48) */
    const val BILL_INQUIRY_MTI = "0100"
    const val BILL_PROCESSING_CODE = "170000"
    const val BILL_POS_ENTRY_MODE = ISO_POS_ENTRY_MODE
    const val BILL_NII = NII
    const val BILL_POS_CONDITION_CODE = POS_CONDITION_CODE
    const val BILL_ID_LENGTH = 13
    const val BILL_PAYMENT_ID_LENGTH = 13

    /** تأییدیه (7-ADVICE) — MTI 0220، DE3 = همان تراکنش اصلی */
    const val ADVICE_MTI = "0220"

    /** برگشت (8-REVERSAL) — MTI 0400، DE3 = همان تراکنش اصلی */
    const val REVERSE_MTI = "0400"

    /** مجوز کارت‌به‌کارت (9-CARD TO CARD TRANSFER AUTHORIZATION) — MTI 0100 / DE3 320000 */
    const val CARD_TO_CARD_AUTH_MTI = "0100"
    /** انتقال کارت‌به‌کارت (10-CARD TO CARD TRANSFER) — MTI 0200 / DE3 320000 */
    const val CARD_TO_CARD_TRANSFER_MTI = "0200"
    const val CARD_TO_CARD_PROCESSING_CODE = "320000"

    /** فعال‌سازی/مقداردهی ترمینال (11-TERMINAL INITIALIZER) — MTI 0800 / DE3 930000 */
    const val TERMINAL_INITIALIZER_MTI = "0800"
    const val TERMINAL_INITIALIZER_PROCESSING_CODE = "930000"

    /** استعلام (12-INQUIRY) — MTI 0100 / DE3 240000، چندپذیرندگی */
    const val INQUIRY_MTI = "0100"
    const val INQUIRY_PROCESSING_CODE = "240000"

    /** عوارض ثابت (13-FIXED DUTY) — MTI 0200 / DE3 220000 */
    const val FIXED_DUTY_MTI = "0200"
    const val FIXED_DUTY_PROCESSING_CODE = "220000"

    const val SUPPORT_MTI = "1100"
    const val SUPPORT_PROCESSING_CODE = "100000"
    const val SUPPORT_FUNCTION_CODE = "702"

    const val OPERATOR_TAG = "018"
    const val MOBILE_TAG = "019"
    const val SUPPORT_SERVICE_TAG = "024"
    const val HOLDER_NAME_TAG = "049"
    const val WALLET_TAG = "045"

    val EMPTY_MAC: ByteArray = ByteArray(8)
}
