package com.example.bill

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.danesh.api.BillInquiryKind

@StringRes
fun BillInquiryKind.titleRes(): Int = when (this) {
    BillInquiryKind.MOBILE -> R.string.bill_inquiry_type_mobile
    BillInquiryKind.TELECOM -> R.string.bill_inquiry_type_telecom
    BillInquiryKind.WATER_ELECTRICITY -> R.string.bill_inquiry_type_utility
}

@StringRes
fun BillInquiryKind.promptRes(): Int = when (this) {
    BillInquiryKind.MOBILE -> R.string.bill_inquiry_enter_mobile
    BillInquiryKind.TELECOM -> R.string.bill_inquiry_enter_line
    BillInquiryKind.WATER_ELECTRICITY -> R.string.bill_inquiry_enter_bill_id
}

@StringRes
fun BillInquiryKind.fieldLabelRes(): Int = when (this) {
    BillInquiryKind.MOBILE -> R.string.bill_inquiry_mobile_label
    BillInquiryKind.TELECOM -> R.string.bill_inquiry_line_label
    BillInquiryKind.WATER_ELECTRICITY -> R.string.bill_label_bill_id
}

@StringRes
fun BillInquiryKind.placeholderRes(): Int = when (this) {
    BillInquiryKind.MOBILE -> R.string.bill_inquiry_mobile_placeholder
    BillInquiryKind.TELECOM -> R.string.bill_inquiry_line_placeholder
    BillInquiryKind.WATER_ELECTRICITY -> R.string.bill_inquiry_bill_id_placeholder
}

@DrawableRes
fun BillInquiryKind.iconRes(): Int = when (this) {
    BillInquiryKind.MOBILE -> R.drawable.ic_bill_id
    BillInquiryKind.TELECOM -> R.drawable.ic_bill_id
    BillInquiryKind.WATER_ELECTRICITY -> R.drawable.ic_water_drop
}

fun parseBillInquiryKind(raw: String): BillInquiryKind? =
    BillInquiryKind.entries.firstOrNull { it.name == raw }
