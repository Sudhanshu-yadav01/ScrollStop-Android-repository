package com.example.scrollstop

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_usage")
data class DailyUsage(
    @PrimaryKey
    val date: String,

    val youtubeMillis: Long = 0L,

    val shortsCount: Int = 0,

    val instagramMillis: Long = 0L,

    val reelsCount: Int = 0
)