package com.danesh.sadad.key

/**
 * ثابت‌های ISO مخصوص سداد.
 * موجودی (2-BALANCE): DE24 = NII‏ 007 — نه Function Code همراه‌پی.
 */
object SadadKeyConfig {
   const val INIT_MTI: String="0800"
    const val INIT__PROCESSING_CODE: String="930000"

    const val MASTER_KEY_HEX = "3132333435363738393A3B3C3D3E3F20"
    const val ENCRYPTED_PIN_KEY_HEX = "5C8A22317AD2DC5A03F674BACDB2BC37"
    const val ENCRYPTED_MAC_KEY_HEX = "F7C6D4AFD6C3832AA16C07D1BA9F62DD"

    const val DEFAULT_TERMINAL_ID = ""//
    const val DEFAULT_MERCHANT_ID = ""//
    const val TERMINAL_ID_LENGTH = 8
    const val MERCHANT_ID_LENGTH = 15

    const val CARD_TO_CARD_POS_ENTRY_MODE = "100010100131"
    const val MERCHANT_TYPE = "6012"
    const val CARDHOLDER_BILLING_CURRENCY = "971"
    const val ASYCUDA = "01000001"

    /** موجودی سداد — MTI 0100 / DE3 310000 (سند 2-BALANCE) */
    const val BALANCE_MTI = "0100"
    const val CARD_TO_CARD_MTI = "0200"
    const val CARD_TO_CARD_PROCESSING_CODE="320000"

    const val BALANCE_PROCESSING_CODE = "310000"
    /** DE22 n 3 */
    const val POS_ENTRY_MODE = "021"
    /** DE24 NII n 3 */
    const val SADAD_NII = "007"
    /** DE25 POS Condition Code n 2 */
    const val POS_CONDITION_CODE = "14"

    /** خرید سداد — MTI 0200 / DE3 000000 (sale / multi-merchant) */
    const val PURCHASE_MTI = "0200"
    const val NAME_INQUIRY_MTI="0100"
    const val NAME_INQUIRY_PROCESSING_CODE="320000"

    const val BILL_INQUIRY_MTI="0100"

    const val BILL_INQUIRY_PROCESSING_CODE="170000"


 const val LOGON_MTI="0800"

 const val LOGON_PROCESSING_CODE="920000"

    /**
     * DE59 پیام INIT — طبق صفحه ۱۹ مستند PosTrans-Final.pdf:
     * Structure Version(n1) + Connection Attempts(n2) + Last time done(n2) +
     * HW(ans5) + SW(ans6) + FW(ans6) + len(n2) + S.NO(LLVAR) +
     * Master Key Index(n3) + Reserve(000) + Enc. Method(n1)
     */
    const val INIT_STRUCTURE_VERSION = "3"
    const val INIT_CONNECTION_ATTEMPTS = "00"
    const val INIT_LAST_TIME_DONE = "00"
    /** ایندکس کلید مستر — در سناریوی فعلی ۱۶ (کاربر موقع تزریق کارت C وارد می‌کند). */
    const val INIT_MASTER_KEY_INDEX = "016"
    const val INIT_RESERVE = "000"
    /** طبق مستند PosTrans-Final.pdf باید ۳ باشد. */
    const val INIT_ENC_METHOD = "3"

    const val PURCHASE_PROCESSING_CODE = "000000"
    /** DE61 Mode 1: one terminal one merchant — Mode n2 + MID n2 */
    const val PURCHASE_DE61_MODE_ONE = "01"
    const val PURCHASE_DEFAULT_MERCHANT_SLOT = "01"

    /** پرداخت قبض سداد — MTI 0200 / DE3 170000 */
    const val BILL_MTI = "0200"
    const val BILL_PROCESSING_CODE = "170000"
    const val BILL_ID_LENGTH = 13
    const val BILL_PAYMENT_ID_LENGTH = 13

    const val VOUCHER_MTI = "1100"
    const val VOUCHER_PROCESSING_CODE = "150000"
    const val VOUCHER_FUNCTION_CODE = "774"

    const val TOPUP_MTI = "0200"
    const val TOPUP_PROCESSING_CODE = "230000"

    const val SUPPORT_MTI = "1100"
    const val SUPPORT_PROCESSING_CODE = "100000"
    const val SUPPORT_FUNCTION_CODE = "702"

    const val REVERSE_MTI = "1420"

    const val OPERATOR_TAG = "018"
    const val MOBILE_TAG = "019"
    const val VOUCHER_AMOUNT_TAG = "010"
    const val SUPPORT_SERVICE_TAG = "024"
    const val HOLDER_NAME_TAG = "049"
    const val WALLET_TAG = "045"

    val EMPTY_MAC: ByteArray = ByteArray(8)
}
