package com.danesh.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.danesh.database.entity.TransactionReportEntity


@Dao
interface TransactionReportDao {
    @Insert
    suspend fun insert(tranData: TransactionReportEntity)

    @Query("SELECT * FROM transaction_report_table WHERE type!=:exceptType and responseCode=:responseCode ORDER BY id DESC LIMIT 1\n")
    fun getLast(exceptType: Int, responseCode: Int): TransactionReportEntity?//, respCode: Int?


    @Query("SELECT * FROM transaction_report_table WHERE stan=:stan")
    fun getByStan(stan: String): TransactionReportEntity?



    @Query("SELECT * FROM transaction_report_table WHERE rrn=:rrn")
    fun getByRrn(rrn: String): TransactionReportEntity?//, respCode: Int?

    @Update
    fun update(transactionLog: TransactionReportEntity)


    @Query("SELECT * FROM transaction_report_table WHERE dateTransaction=:date_ and timeTransaction=:time")
    fun getByDateTime(date_: String, time: String): TransactionReportEntity?


    @Query("SELECT * FROM transaction_report_table WHERE dateTransaction<=:fromDate and dateTransaction>:toDate" +
            " and amount<=:fromAmount and amount>:toAmount and type IN (:selectedTransactions)")
    fun getInRangeDate(
        fromDate: String,
        toDate: String,
        fromAmount: Long,
        toAmount: Long,
        selectedTransactions: List<Int>,
    ): List<TransactionReportEntity>?


//    @Query("SELECT * FROM transaction_log WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT 1\n")
//    suspend fun getInRangeDate(from: Long, to: Long): List<TransactionLogEntity>


//    @Query("SELECT * FROM transaction_log WHERE timestamp>:from AND timestamp<:to ORDER BY timestamp "+
//            " and amount<=:fromAmount and amount>:toAmount and type IN (:selectedTransactions)")
//    suspend fun getInRangeDate(from: Long, to: Long,
//                               fromAmount: Long,
//                               toAmount: Long,
//                               selectedTransactions: List<Int>,): List<TransactionLogEntity>

    @Query("SELECT * FROM transaction_report_table WHERE  type IN (:selectedTransactions)")
    suspend fun getInRangeDate(
        selectedTransactions: List<Int>,): List<TransactionReportEntity>


    @Query("SELECT * FROM transaction_report_table \n" +
            "    WHERE timestamp >= :from \n" +
            "    AND timestamp <= :to \n" +
            "    AND amount >= :fromAmount \n" +
            "    AND amount <= :toAmount \n" +
            "    AND type IN (:selectedTransactions) \n" +
            "    ORDER BY timestamp DESC")
    suspend fun getInRangeDate(from: Long, to: Long,
                               fromAmount: Long,
                               toAmount: Long,
                               selectedTransactions: List<Int>,): List<TransactionReportEntity>


    @Query("SELECT MAX(amount) FROM transaction_report_table")
    suspend fun getMaxAmount(): Long?

    @Query("SELECT * FROM transaction_report_table ORDER BY timestamp DESC, id DESC")
    fun getAll():List<TransactionReportEntity>

    @Query(
        """
        DELETE FROM transaction_report_table
        WHERE id IN (
            SELECT id FROM transaction_report_table
            ORDER BY timestamp ASC, id ASC
            LIMIT :count
        )
        """,
    )
    suspend fun deleteOldest(count: Int): Int

    @Query("DELETE  FROM transaction_report_table WHERE dateTransaction=:date and timeTransaction=:time")
    fun deleteByDateTime(date: String, time: String)


    @Query("DELETE FROM transaction_report_table")
    suspend fun clearTable()
}