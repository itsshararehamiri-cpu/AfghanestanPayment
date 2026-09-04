package com.danesh.database.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "store_forward_queue_table"
)
data class StoreForwardQueueEntity(
    @NonNull @PrimaryKey var dateTime: String,
    var date: String,
    var time: String,
    /**
     * چرخه عمر SAF:
     * 'R' = هنوز نهایی نشده (Reverse در صورت نیاز)،
     * 'S' = نهایی شده و منتظر Advice.
     * @see com.danesh.api.SafStatuses
     */
    var status: Char,
    /** وضعیت چاپ رسید مشتری برای این تراکنش SAF */
    @ColumnInfo(name = "printed")
    var customerReceiptPrinted: Boolean,
    var processingCode: String,
    var amount: String,
    var stan: String,
    var merchantId: String,
    var maskedPan: String?,
    var type: Int,
    var rrn: String?,
    var issuer: String?,
    var responseCode: Int?,
    var responseMsg: String?,
    var terminalId: String,
    var posConditionCode:String,
    var currency:String,
    /** هم‌تراز با [status]: 'A' = advice، 'R' = reverse — see [com.danesh.api.QueueOperations] */
    var queueOperation: Char = 'R',
    /** PAN مبدأ برای Reverse (DE2) — فقط SAF */
    var sourcePan: String? = null,
    /** Function Code اصلی (DE24) برای Reverse — مثلاً 689 / 781 */
    var functionCode: String? = null,
    /** تگ مقصد DE48 برای Reverse — 021 یا 045 */
    var reverseDestTag: String? = null,
    /** مقدار مقصد DE48 برای Reverse — PAN مقصد یا کد کیف پول */
    var reverseDestValue: String? = null,
)