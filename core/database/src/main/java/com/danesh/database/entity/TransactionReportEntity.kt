package com.danesh.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey




@Entity(
    tableName = "transaction_report_table"
)
data class TransactionReportEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    val timestamp:Long,
    var processingCode: String,
    var amount: Long,
    var stan: String,
    var dateTransaction: String,
    var timeTransaction: String,
    var merchantId: String,
    var maskedPan: String?,
    var type: Int,
    var rrn: String?,
    var issuer: String?,
    var responseCode: Int?,
    var responseMsg: Int?,
    var billId: String? = null,
    var payId: String? = null,
    var serviceDesc: String? = null,
    var pinVoucher: String? = null,
    var serialVoucher: String? = null,
    var mobileNumber: String? = null,
    var operatorCode: Int? = null,
    var destinationPan: String? = null,
    var walletCode: String? = null,
    var terminalId: String,
    val merchantName: String = "",
    val merchantPhone: String? = null
) {}