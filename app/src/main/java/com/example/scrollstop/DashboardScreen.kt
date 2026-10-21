
package com.example.scrollstop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import androidx.compose.foundation.background
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment


import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.util.Date
import java.util.Locale

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.scrollstop.ui.theme.HeadingFont


private val DashboardBlack = Color(0xFF171714)
private val DashboardCard = Color(0xFFFAF8F2)
private val DashboardGreen = Color(0xFF171714)
private val DashboardWhite = Color(0xFF171714)
private val DashboardGray = Color(0xFF77736A)
private val DashboardCream = Color(0xFFF3F0E8)
private val DashboardBeige = Color(0xFFD7D0C4)


@Composable
fun DashboardScreen(viewModel: UsageViewModel) {
    val context = LocalContext.current
    val records by viewModel.allUsage.collectAsState()

    var selectedPeriod by remember { mutableStateOf("Today") }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayCalendar = Calendar.getInstance()
    val todayString = dateFormat.format(todayCalendar.time)

    val weekStart = todayCalendar.clone() as Calendar
    val dayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK)
    val daysSinceMonday = (dayOfWeek + 5) % 7
    weekStart.add(Calendar.DAY_OF_MONTH, -daysSinceMonday)

    val monthStart = todayCalendar.clone() as Calendar
    monthStart.set(Calendar.DAY_OF_MONTH, 1)

    val yearStart = todayCalendar.clone() as Calendar
    yearStart.set(Calendar.DAY_OF_YEAR, 1)

    val weekStartString = dateFormat.format(weekStart.time)
    val monthStartString = dateFormat.format(monthStart.time)
    val yearStartString = dateFormat.format(yearStart.time)

    val filteredRecords = records.filter { record ->
        when (selectedPeriod) {
            "Today" -> record.date == todayString
            "Week" -> record.date >= weekStartString &&
                    record.date <= todayString
            "Month" -> record.date >= monthStartString &&
                    record.date <= todayString
            "Year" -> record.date >= yearStartString &&
                    record.date <= todayString
            else -> false
        }
    }

    val chartEntries: List<Pair<String, Long>> =
        when (selectedPeriod) {
            "Today" -> listOf(
                "YT" to filteredRecords.sumOf { it.youtubeMillis },
                "IG" to filteredRecords.sumOf { it.instagramMillis }
            )

            "Week" -> (0..6).map { offset ->
                val day = (weekStart.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, offset)
                }

                val key = dateFormat.format(day.time)
                val dayRecords = records.filter { it.date == key }

                val label = day.getDisplayName(
                    Calendar.DAY_OF_WEEK,
                    Calendar.SHORT,
                    Locale.getDefault()
                ) ?: ""

                label to dayRecords.sumOf {
                    it.youtubeMillis + it.instagramMillis
                }
            }

            "Month" -> (1..todayCalendar.get(Calendar.DAY_OF_MONTH)).map { dayNumber ->
                val day = (monthStart.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, dayNumber)
                }

                val key = dateFormat.format(day.time)
                val dayRecords = records.filter { it.date == key }

                val label =
                    if (dayNumber == 1 ||
                        dayNumber % 7 == 0 ||
                        dayNumber == todayCalendar.get(Calendar.DAY_OF_MONTH)
                    ) {
                        dayNumber.toString()
                    } else {
                        ""
                    }

                label to dayRecords.sumOf {
                    it.youtubeMillis + it.instagramMillis
                }
            }

            else -> (0..todayCalendar.get(Calendar.MONTH)).map { monthIndex ->
                val month = (yearStart.clone() as Calendar).apply {
                    set(Calendar.MONTH, monthIndex)
                }

                val prefix = SimpleDateFormat(
                    "yyyy-MM",
                    Locale.getDefault()
                ).format(month.time)

                val monthRecords = records.filter {
                    it.date.startsWith(prefix)
                }

                val label = month.getDisplayName(
                    Calendar.MONTH,
                    Calendar.SHORT,
                    Locale.getDefault()
                ) ?: ""

                label to monthRecords.sumOf {
                    it.youtubeMillis + it.instagramMillis
                }
            }
        }

    val youtubeMillis = filteredRecords.sumOf { it.youtubeMillis }
    val shortsCount = filteredRecords.sumOf { it.shortsCount }
    val instagramMillis = filteredRecords.sumOf { it.instagramMillis }
    val reelsCount = filteredRecords.sumOf { it.reelsCount }

    fun formatDuration(millis: Long): String {
        val totalMinutes = millis / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return if (hours > 0) "${hours}h ${minutes}m"
        else "${minutes}m"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardCream),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ScrollStop",
                    color = DashboardBlack,
                    fontSize = 30.sp,
                    fontFamily = HeadingFont
                )

                IconButton(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        )
                    }
                ) {
                    Text(
                        text = "⚙",
                        fontSize = 24.sp,
                        color = DashboardGreen
                    )
                }
            }
        }

        // Subtitle
        item {
            Text(
                text = "YOUR DIGITAL WELLNESS",
                color = DashboardGray,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
        }

        // Period filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Today", "Week", "Month", "Year").forEach { period ->
                    androidx.compose.material3.FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = {
                            Text(
                                text = period,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.FilterChipDefaults
                            .filterChipColors(
                                selectedContainerColor = DashboardBlack,
                                selectedLabelColor = DashboardCream,
                                containerColor = DashboardCard,
                                labelColor = DashboardGray
                            ),
                        border = null
                    )
                }
            }
        }

        // YouTube and Shorts cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "YouTube time",
                    value = formatDuration(youtubeMillis),
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Shorts detected",
                    value = shortsCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Instagram and Reels cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Instagram time",
                    value = formatDuration(instagramMillis),
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Reels detected",
                    value = reelsCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Chart
        item {
            UsageChart(
                entries = chartEntries,
                totalMillis = chartEntries.sumOf { it.second },
                formatDuration = ::formatDuration
            )
        }

        // History heading
        item {
            Text(
                text = "DAILY HISTORY",
                color = DashboardWhite,
                fontSize = 24.sp,
                fontFamily = HeadingFont
            )
        }

        // History cards
        if (filteredRecords.isEmpty()) {
            item {
                Text(
                    text = "No usage saved for this period yet.",
                    color = DashboardGray
                )
            }
        } else {
            items(
                filteredRecords,
                key = { it.date }
            ) { record ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = DashboardCard
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = record.date,
                            color = DashboardGreen,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "YouTube: ${formatDuration(record.youtubeMillis)}",
                            color = DashboardWhite
                        )

                        Text(
                            text = "Shorts detected: ${record.shortsCount}",
                            color = DashboardGray
                        )

                        Text(
                            text = "------------------------------------" ,
                            color = DashboardGray
                        )

                        Text(
                            text = "Instagram: ${formatDuration(record.instagramMillis)}",
                            color = DashboardWhite
                        )

                        Text(
                            text = "Reels detected: ${record.reelsCount}",
                            color = DashboardGray
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = DashboardCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DashboardCard)
                .padding(16.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = DashboardGray,
                fontSize = 11.sp,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = value,
                color = DashboardGreen,
                fontSize = 22.sp,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "THIS PERIOD",
                color = Color(0xFF69736A),
                fontSize = 9.sp
            )
        }
    }
}


@Composable
private fun UsageChart(
    entries: List<Pair<String, Long>>,
    totalMillis: Long,
    formatDuration: (Long) -> String
) {
    val maxValue = (entries.maxOfOrNull { it.second } ?: 0L).coerceAtLeast(1L)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = DashboardCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DashboardBeige)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "USAGE OVERVIEW",
                color = DashboardGray,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = formatDuration(totalMillis),
                color = DashboardBlack,
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Total recorded usage",
                color = DashboardGray,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                entries.forEach { entry ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        val barHeight = if (entry.second > 0L) {
                            (78f * entry.second.toFloat() / maxValue.toFloat())
                                .coerceAtLeast(4f)
                        } else {
                            3f
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(barHeight.dp)
                                .background(
                                    if (entry.second > 0L) DashboardBlack
                                    else DashboardBeige,
                                    MaterialTheme.shapes.small
                                )
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = entry.first,
                            color = DashboardGray,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
