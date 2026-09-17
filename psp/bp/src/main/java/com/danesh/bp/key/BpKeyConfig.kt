package com.danesh.bp.key

import com.danesh.bp.field48.BpField48Tags


object BpKeyConfig {
    const val PROJECT_CODE ="1007"// "00001007"
    const val INITIAL_MASTER_KEY_HEX = "32785F7FC3369FBE86ED16A9AA39EA54F7D070ED3FD7923E"
    const val INITIAL_MAC_KEY_HEX = INITIAL_MASTER_KEY_HEX
    const val INIT_STAN = "000320"
    const val INIT_MESSAGE_REASON = "10"
    const val LOGON_STAN = "000320"
    const val LOGON_MESSAGE_REASON = "10"


    const val NETWORK_FIELD53 = PROJECT_CODE


    const val FIELD53 = PROJECT_CODE

    const val BALANCE_MTI = "0200"
    const val BILL_MTI = "0200"

    const val BALANCE_PROCESSING_CODE = "310000"

    const val BILL_PROCESSING_CODE = "170000"

    const val BALANCE_STAN = "000290"
    const val BALANCE_MESSAGE_REASON = "10"
    const val BALANCE_CURRENCY = "364"
    const val BALANCE_FIELD48_TAG = BpField48Tags.LAST_SUCCESS_STAN
    const val BILL_ID_FIELD48_TAG= BpField48Tags.BILL_ID
    const val PAY_ID_FIELD48_TAG= BpField48Tags.PAYMENT_ID

    const val DEFAULT_LAST_SUCCESS_STAN = "000000"
    const val DEFAULT_LAST_SUCCESS_RRN = "000000000000"
    const val LAST_SUCCESS_STAN_LENGTH = 6
    const val LAST_SUCCESS_RRN_LENGTH = 12

    const val BALANCE_FIELD48_VALUE = DEFAULT_LAST_SUCCESS_STAN

    const val PURCHASE_MTI = "0200"
    const val PURCHASE_PROCESSING_CODE = "000000"
    const val PURCHASE_STAN = "000298"
    const val PURCHASE_MESSAGE_REASON = "10"
    const val PURCHASE_CURRENCY = "364"
    const val PURCHASE_FIELD48_TAG = BpField48Tags.LAST_SUCCESS_STAN
    const val PURCHASE_FIELD48_VALUE = DEFAULT_LAST_SUCCESS_STAN

    const val ADVICE_MTI = "0220"
    const val ADVICE_FIELD48_TAG = BpField48Tags.LAST_SUCCESS_STAN
    const val ADVICE_FIELD48_VALUE = DEFAULT_LAST_SUCCESS_STAN

    const val REVERSE_MTI = "0400"
    const val REVERSE_DEFAULT_RRN = DEFAULT_LAST_SUCCESS_RRN
    const val REVERSE_FIELD48_TAG = BpField48Tags.LAST_SUCCESS_STAN
    const val REVERSE_FIELD48_VALUE = DEFAULT_LAST_SUCCESS_STAN
    const val REVERSE_FIELD48_TAG_CANCEL = BpField48Tags.CANCEL_REASON

    const val VOUCHER_MTI = "0200"
    const val TOPUP_MTI = "0200"

    const val VOUCHER_PROCESSING_CODE = "150000"
    const val TOPUP_PROCESSING_CODE = "230000"

    const val VOUCHER_STAN = "000318"
    const val VOUCHER_MESSAGE_REASON = "10"
    const val VOUCHER_CURRENCY = "364"
    const val VOUCHER_FIELD48_TAG = BpField48Tags.LAST_SUCCESS_STAN
    const val VOUCHER2_FIELD48_TAG= BpField48Tags.LAST_SUCCESS_RRN
    const val VOUCHER_FIELD48_VALUE = DEFAULT_LAST_SUCCESS_STAN
    const val VOUCHER_FIELD48_TAG_MOBILE = BpField48Tags.MOBILE_NUMBER
    const val TOP_UP_FIELD48_TAG_SERVICE_CODE= BpField48Tags.SERVICE

    const val VOUCHER_FIELD48_TAG_OPERATOR = BpField48Tags.MOBILE_OPERATOR
    const val VOUCHER_FIELD48_TAG_AMOUNT = BpField48Tags.VOUCHER_AMOUNT

    const val SUPPORT_MTI = "0200"
    const val SUPPORT_PROCESSING_CODE = "100000"
    const val SUPPORT_MESSAGE_REASON = "10"
    const val SUPPORT_CURRENCY = "364"
    const val SUPPORT_FIELD48_TAG_STAN = BpField48Tags.LAST_SUCCESS_STAN
    const val SUPPORT_FIELD48_VALUE_STAN = DEFAULT_LAST_SUCCESS_STAN
    const val SUPPORT_FIELD48_TAG_SERVICE = BpField48Tags.SUPPORT_MENU_ITEMS

    const val COUPON_REQUEST_INDEX_TAG  = BpField48Tags.COUPON_REQUEST_INDEX_TAG


    const val CASH_DEPOSIT_MTI = "1100"
    const val CASH_DEPOSIT_PROCESSING_CODE = "210000"
    const val CASH_DEPOSIT_STAN = "000300"
    const val CASH_DEPOSIT_MESSAGE_REASON = "10"
    const val CASH_DEPOSIT_CURRENCY = "364"
    const val CASH_DEPOSIT_NII = "618"

    const val CASH_OUT_MTI = "1100"
    const val CASH_OUT_PROCESSING_CODE = "010000"
    const val CASH_OUT_STAN = "000301"
    const val CASH_OUT_MESSAGE_REASON = "10"
    const val CASH_OUT_CURRENCY = "364"
    const val CASH_OUT_NII = "700"
    const val COUPON_MTI="0100"
    const val COUPON_PROCESSING_CODE = "820000"


    const val COUPON_INQUIRY_MTI="0100"
    const val COUPON_INQUIRY_PROCESSING_CODE = "810000"

    const val COUPON_PURCHASE_MTI = "0200"
    const val COUPON_PURCHASE_PROCESSING_CODE = "800000"
    const val COUPON_PURCHASE_CURRENCY = "364"

    /** ارسال تگ 04 یا 05 الزامی است — در تمام تراکنش‌های کالابرگ از تگ 04 (آخرین STAN موفق) استفاده می‌شود. */
    const val COUPON_FIELD48_TAG_LAST_SUCCESS = BpField48Tags.LAST_SUCCESS_STAN
    const val COUPON_LAST_INDEX_TAG = BpField48Tags.COUPON_LAST_INDEX_TAG
    const val COUPON_CREDIT_BALANCE_TAG = BpField48Tags.COUPON_CREDIT_BALANCE_TAG
    const val COUPON_CREDIT_REQUIRED_TAG = BpField48Tags.COUPON_CREDIT_REQUIRED_TAG

    /**
     * مقدار قراردادی [com.danesh.api.QueueItem.reverseDestTag] برای علامت‌گذاری اینکه
     * [com.danesh.api.QueueItem.reverseDestValue] باید در فیلد 44 پیام Reverse/Advice
     * (نه DE48) قرار گیرد — برای حفظ «شماره پیگیری کالابرگ» در Reverse/Confirm خرید کالابرگ.
     */
    const val QUEUE_FIELD44_DEST_TAG = "044"


    const val INIT_MTI = "0800"
    const val INIT_PROCESSING_CODE = "900000"
    const val LOGON_MTI = "0800"
    const val  LOGON_PROCESSING_CODE = "920000"



}
