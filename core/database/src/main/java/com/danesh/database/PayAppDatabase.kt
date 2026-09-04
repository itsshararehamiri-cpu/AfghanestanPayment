package com.danesh.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.danesh.database.dao.StoreForwardQueueDao
import com.danesh.database.dao.TransactionReportDao
import com.danesh.database.entity.StoreForwardQueueEntity
import com.danesh.database.entity.TransactionReportEntity

@Database(
    entities = [
        StoreForwardQueueEntity::class,
        TransactionReportEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class PayAppDatabase : RoomDatabase() {
    abstract fun storeForwardQueueDao(): StoreForwardQueueDao
    abstract fun transactionReportDao(): TransactionReportDao
}
