package com.danesh.database.di


import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.danesh.database.PayAppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val PAY_DATABASE_NAME = "pay_database"

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Provides
    @Singleton
    fun provideDataBase(@ApplicationContext context: Context) : PayAppDatabase {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS 'log_table' (
                'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                'type' INTEGER NOT NULL,
                'number' INTEGER NOT NULL
            )
        """.trimIndent()
                )
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Room stores Char as INTEGER 82 = 'R'
                database.execSQL(
                    "ALTER TABLE store_forward_queue_table ADD COLUMN queueOperation INTEGER NOT NULL DEFAULT 82",
                )
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE store_forward_queue_table ADD COLUMN sourcePan TEXT",
                )
                database.execSQL(
                    "ALTER TABLE store_forward_queue_table ADD COLUMN functionCode TEXT",
                )
                database.execSQL(
                    "ALTER TABLE store_forward_queue_table ADD COLUMN reverseDestTag TEXT",
                )
                database.execSQL(
                    "ALTER TABLE store_forward_queue_table ADD COLUMN reverseDestValue TEXT",
                )
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE transaction_report_table ADD COLUMN destinationPan TEXT",
                )
                database.execSQL(
                    "ALTER TABLE transaction_report_table ADD COLUMN walletCode TEXT",
                )
            }
        }
        return Room.databaseBuilder(
            context,
            PayAppDatabase::class.java, PAY_DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }



}

