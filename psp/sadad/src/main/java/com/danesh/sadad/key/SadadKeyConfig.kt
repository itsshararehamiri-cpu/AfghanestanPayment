package com.danesh.sadad.key

/**
 * ثابت‌های ISO مخصوص سداد.
 * موجودی (2-BALANCE): DE24 = NII‏ 007 — نه Function Code همراه‌پی.
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
    const val ASYCUDA = "01000001"

    /** موجودی سداد — MTI 0100 / DE3 310000 (سند 2-BALANCE) */
    const val BALANCE_MTI = "0100"
    const val BALANCE_PROCESSING_CODE = "310000"
    /** DE22 n 3 */
    const val BALANCE_POS_ENTRY_MODE = "021"
    /** DE24 NII n 3 */
    const val BALANCE_NII = "007"
    /** DE25 POS Condition Code n 2 */
    const val BALANCE_POS_CONDITION_CODE = "14"

    /** خرید سداد — MTI 0200 / DE3 000000 (sale / multi-merchant) */
    const val PURCHASE_MTI = "0200"
    const val PURCHASE_PROCESSING_CODE = "000000"
    const val PURCHASE_POS_ENTRY_MODE = "021"
    const val PURCHASE_NII = "007"
    const val PURCHASE_POS_CONDITION_CODE = "14"
    /** DE61 Mode 1: one terminal one merchant — Mode n2 + MID n2 */
    const val PURCHASE_DE61_MODE_ONE = "01"
    const val PURCHASE_DEFAULT_MERCHANT_SLOT = "01"

    /** پرداخت قبض سداد — MTI 0200 / DE3 170000 */
    const val BILL_MTI = "0200"
    const val BILL_PROCESSING_CODE = "170000"
    const val BILL_POS_ENTRY_MODE = "021"
    const val BILL_NII = "007"
    const val BILL_POS_CONDITION_CODE = "14"
    const val BILL_ID_LENGTH = 13
    const val BILL_PAYMENT_ID_LENGTH = 13

    const val VOUCHER_MTI = "1100"
    const val VOUCHER_PROCESSING_CODE = "150000"
    const val VOUCHER_FUNCTION_CODE = "774"

    const val TOPUP_MTI = "1100"
    const val TOPUP_PROCESSING_CODE = "230000"
    const val TOPUP_FUNCTION_CODE = "774"

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
