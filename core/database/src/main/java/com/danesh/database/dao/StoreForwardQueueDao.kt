package com.danesh.database.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.danesh.database.entity.StoreForwardQueueEntity

@Dao
interface StoreForwardQueueDao {
    @Insert
    suspend fun insert(storeForwardQueueEntity: StoreForwardQueueEntity)

    @Query("DELETE  FROM store_forward_queue_table WHERE date=:date and time=:time")
    fun deleteByDateTime(date: String, time: String)

    @Query("SELECT * FROM store_forward_queue_table WHERE date=:date and time=:time")
    fun getByDateTime(date: String, time: String): StoreForwardQueueEntity?

    @Query("SELECT * FROM store_forward_queue_table")
    fun getAll(): List<StoreForwardQueueEntity>?
    @Query("UPDATE store_forward_queue_table SET status=:status WHERE date=:date AND time=:time")
    fun updateStatusByDateTime(status: Char, date: String, time: String)

    @Query(
        """
        UPDATE store_forward_queue_table
        SET status=:status, queueOperation=:queueOperation, rrn=:rrn
        WHERE date=:date AND time=:time
        """,
    )
    fun confirmByDateTime(
        status: Char,
        queueOperation: Char,
        rrn: String?,
        date: String,
        time: String,
    )

    @Query("SELECT * FROM store_forward_queue_table WHERE printed=:customerReceiptPrinted ORDER BY dateTime ASC LIMIT 1")
    fun getFirstPendingByCustomerReceiptPrinted(customerReceiptPrinted: Boolean): StoreForwardQueueEntity?

    @Query("update store_forward_queue_table SET printed=:customerReceiptPrinted where date=:date and time=:time")
    fun updateCustomerReceiptPrintStatus(customerReceiptPrinted: Boolean, date: String, time: String)

    @Query("DELETE   FROM store_forward_queue_table ")
    fun deleteAll()
}