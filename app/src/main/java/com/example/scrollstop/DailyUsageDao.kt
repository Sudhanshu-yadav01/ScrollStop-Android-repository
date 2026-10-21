package com.example.scrollstop

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyUsageDao {

    @Upsert
    suspend fun saveDailyUsage(usage: DailyUsage)

    @Query("SELECT * FROM daily_usage WHERE date = :date")
    fun getUsageForDate(date: String): Flow<DailyUsage?>

    @Query("SELECT * FROM daily_usage ORDER BY date DESC")
    fun getAllUsage(): Flow<List<DailyUsage>>

    @Query("SELECT * FROM daily_usage WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getUsageBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<DailyUsage>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun ensureDailyUsage(usage: DailyUsage)

    @Query("""
    UPDATE daily_usage
    SET youtubeMillis = youtubeMillis + :millis
    WHERE date = :date
""")
    suspend fun addYouTubeTime(date: String, millis: Long)

    @Query("""
    UPDATE daily_usage
    SET shortsCount = shortsCount + 1
    WHERE date = :date
""")
    suspend fun incrementShortsCount(date: String)

    @Query("""
    UPDATE daily_usage
    SET instagramMillis = instagramMillis + :millis
    WHERE date = :date
""")
    suspend fun addInstagramTime(date: String, millis: Long)

    @Query("""
    UPDATE daily_usage
    SET reelsCount = reelsCount + 1
    WHERE date = :date
""")
    suspend fun incrementReelsCount(date: String)
}