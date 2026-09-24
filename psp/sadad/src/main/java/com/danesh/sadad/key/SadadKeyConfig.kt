package com.danesh.sadad.key

/**
 * ثابت‌های ISO مخصوص سداد.
 * موجودی (2-BALANCE): DE24 = NII‏ 007 — نه Function Code همراه‌پی.
 */
object SadadKeyConfig {
   const val INIT_MTI: String="0800"
    const val INIT__PROCESSING_CODE: String="930000"

    /** اسلات PED برای Init MAC (کلید 04) — INIT و LOGON فقط با این کلید MAC می‌شوند. */
    const val INIT_MAC_PED_INDEX = 4

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
    const val INIT_ENC_METHOD = "4"

    const val PURCHASE_PROCESSING_CODE = "000000"
    /** DE61 Mode 1: one terminal one merchant — Mode n2 + MID n2 */
    const val PURCHASE_DE61_MODE_ONE = "01"
    const val PURCHASE_DEFAULT_MERCHANT_SLOT = "01"

    /** پرداخت قبض سداد — MTI 0200 / DE3 170000 */
    const val BILL_MTI = "0200"
    const val BILL_PROCESSING_CODE = "170000"
    const val BILL_ID_LENGTH = 13
    /** شناسه پرداخت ۶ تا ۱۳ رقم است و در DE48 تا ۱۳ رقم از چپ با صفر پر می‌شود. */
    const val BILL_PAYMENT_ID_LENGTH = 13
    /**
     * استعلام قبض — پاورقی سند: Using function code 8.
     * در DE63 به‌صورت n3 می‌رود.
     */
    const val BILL_INQUIRY_FUNCTION_CODE = "008"

    /** 5-CHARGE سند: MTI 0200 (پاسخ 0210) — قبلاً به‌اشتباه 1100 بود. */
    const val VOUCHER_MTI = "0200"
    const val VOUCHER_PROCESSING_CODE = "150000"
    const val VOUCHER_FUNCTION_CODE = "774"

    const val TOPUP_MTI = "0200"
    const val TOPUP_PROCESSING_CODE = "230000"
    /** DE63 Function Code شارژ مستقیم — operator/category/service + موبایل. */
    const val TOPUP_FUNCTION_CODE = "006"

    const val SUPPORT_MTI = "1100"
    const val SUPPORT_PROCESSING_CODE = "100000"
    const val SUPPORT_FUNCTION_CODE = "702"

    /** 8-ADVICE: 0220 (پاسخ 0230) */
    const val ADVICE_MTI = "0220"
    /** 9-REVERSAL: 0400 (پاسخ 0410) */
    const val REVERSE_MTI = "0400"

    const val OPERATOR_TAG = "018"
    const val MOBILE_TAG = "019"
    const val VOUCHER_AMOUNT_TAG = "010"
    const val SUPPORT_SERVICE_TAG = "024"
    const val HOLDER_NAME_TAG = "049"
    const val WALLET_TAG = "045"

    /** 10-INQUIRY: MTI 0100 (پاسخ 0110) / DE3 240000 */
    const val INQUIRY_MTI = "0100"
    const val INQUIRY_PROCESSING_CODE = "240000"

    /** 11-FIXED DUTY: MTI 0200 (پاسخ 0210) / DE3 220000 */
    const val FIXED_DUTY_MTI = "0200"
    const val FIXED_DUTY_PROCESSING_CODE = "220000"

    /** 13-INQUIRY STATUS: MTI 0100 (پاسخ 0110) / DE3 330000 */
    const val INQUIRY_STATUS_MTI = "0100"
    const val INQUIRY_STATUS_PROCESSING_CODE = "330000"
    /** DE48 مقدار ثابت طبق سند */
    const val INQUIRY_STATUS_FIELD48 = "01"

    /** 14-INQUIRY BNPL: MTI 0100 (پاسخ 0110) / DE3 690000 */
    const val INQUIRY_BNPL_MTI = "0100"
    const val INQUIRY_BNPL_PROCESSING_CODE = "690000"

    /** 15/16/17 کالابرگ: MTI متفاوت با DE3 مشترک 680000 */
    const val COMMODITY_BASKET_PROCESSING_CODE = "680000"
    const val INQUIRY_COMMODITY_BASKET_MTI = "0100"
    const val SALE_COMMODITY_BASKET_MTI = "0200"
    const val CANCEL_COMMODITY_BASKET_MTI = "0100"

    /** 18-TRANSACTION SUMMARY: MTI 0100 (پاسخ 0110) / DE3 430000 */
    const val TRANSACTION_SUMMARY_MTI = "0100"
    const val TRANSACTION_SUMMARY_PROCESSING_CODE = "430000"

    /** 19-ACCEPT PIN: MTI 0100 (پاسخ 0110) / DE3 710000 */
    const val ACCEPT_PIN_MTI = "0100"
    const val ACCEPT_PIN_PROCESSING_CODE = "710000"

    /** 20-REFUND: MTI 0200 (پاسخ 0210) / DE3 200000 */
    const val REFUND_MTI = "0200"
    const val REFUND_PROCESSING_CODE = "200000"
    const val REFUND_TYPE_OFFLINE = "01"
    const val REFUND_TYPE_ONLINE = "02"

    /** 21.1-KAHROBA SALE: MTI 0200 (پاسخ 0210) / DE3 000000 / DE22 071 (NFC) */
    const val KAHROBA_POS_ENTRY_MODE = "071"
    /** 21.2-KAHROBA BALANCE: MTI 0100 (پاسخ 0110) / DE3 310000 / DE22 071 (NFC) */

    /** 22-GAM BOND: MTI 0200 (پاسخ 0210) / DE3 700000 */
    const val GAM_BOND_MTI = "0200"
    const val GAM_BOND_PROCESSING_CODE = "700000"

    /** 23-BAAM WALLET INQUIRY: MTI 0100 (پاسخ 0110) / DE3 240000 */
    const val BAAM_WALLET_MTI = "0100"
    const val BAAM_WALLET_PROCESSING_CODE = "240000"
    const val BAAM_WALLET_FUNCTION_CODE = "086"

    /** 24-SALE GIS STATION: MTI 0200 (پاسخ 0210) / DE3 740000 */
    const val SALE_GIS_STATION_MTI = "0200"
    const val SALE_GIS_STATION_PROCESSING_CODE = "740000"
    const val GIS_STATION_FUNCTION_CODE = "060"

    /** 25.1-INQUIRY TOLL: MTI 0100 (پاسخ 0110) / DE3 240000 */
    const val TOLL_INQUIRY_MTI = "0100"
    const val TOLL_INQUIRY_PROCESSING_CODE = "240000"
    /** 25.2-TOLL PAYMENT: MTI 0200 (پاسخ 0210) / DE3 730000 */
    const val TOLL_PAYMENT_MTI = "0200"
    const val TOLL_PAYMENT_PROCESSING_CODE = "730000"

    /** 26-FUEL STATION INQUIRY: MTI 0100 (پاسخ 0110) / DE3 240000 */
    const val FUEL_STATION_INQUIRY_MTI = "0100"
    const val FUEL_STATION_INQUIRY_PROCESSING_CODE = "240000"

    val EMPTY_MAC: ByteArray = ByteArray(8)

    /** DE63 Client/Host: Portable بدون داده — مقدمهٔ پروتکل (`01040000`). */
    const val FUNCTION_CODE_CONNECTION: String = "040"

    /** DE63 Host: Terminal initializer (اطلاعات پایه پایانه/پذیرنده در پاسخ INIT). */
    const val FUNCTION_CODE_TERMINAL_INITIALIZER: String = "013"
}
