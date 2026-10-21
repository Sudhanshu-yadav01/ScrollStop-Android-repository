package com.example.scrollstop

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyUsage::class],
    version = 1,
    exportSchema = false
)
abstract class ScrollStopDatabase : RoomDatabase() {

    abstract fun dailyUsageDao(): DailyUsageDao

    companion object {
        @Volatile
        private var INSTANCE: ScrollStopDatabase? = null

        fun getDatabase(context: Context): ScrollStopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScrollStopDatabase::class.java,
                    "scrollstop_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}