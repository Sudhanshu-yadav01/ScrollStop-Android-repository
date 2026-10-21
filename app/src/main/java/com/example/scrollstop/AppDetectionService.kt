
package com.example.scrollstop

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log

class AppDetectionService : AccessibilityService() {

    private var lastShortsDescription = ""
    private var lastShortsCountTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var lastDetectedPackage: String? = null

    private val checkForegroundApp = object : Runnable {
        override fun run() {
            try {
                val usageStatsManager =
                    getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

                val endTime = System.currentTimeMillis()
                val startTime = endTime - 10_000

                val events = usageStatsManager.queryEvents(startTime, endTime)
                val event = UsageEvents.Event()
                var foregroundPackage: String? = null
                var latestTimestamp = 0L

                while (events.hasNextEvent()) {
                    events.getNextEvent(event)

                    val isResumed =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            event.eventType ==
                                    UsageEvents.Event.ACTIVITY_RESUMED
                        } else {
                            @Suppress("DEPRECATION")
                            event.eventType ==
                                    UsageEvents.Event.MOVE_TO_FOREGROUND
                        }

                    if (isResumed && event.timeStamp >= latestTimestamp) {
                        latestTimestamp = event.timeStamp
                        foregroundPackage = event.packageName
                    }
                }

                if (
                    foregroundPackage != null &&
                    foregroundPackage != lastDetectedPackage
                ) {
                    lastDetectedPackage = foregroundPackage

                    Log.d(
                        "ScrollStopDetect",
                        "Foreground app: $foregroundPackage"
                    )

                    val intent = Intent(
                        this@AppDetectionService,
                        FloatingTimerService::class.java
                    ).apply {
                        action = FloatingTimerService.ACTION_TRACK_APP
                        putExtra(
                            FloatingTimerService.EXTRA_PACKAGE_NAME,
                            foregroundPackage
                        )
                    }

                    startService(intent)
                }
            } catch (e: Exception) {
                Log.e("ScrollStopDetect", "Foreground detection failed", e)
            }

            handler.postDelayed(this, 500)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        if (!hasUsageAccess()) {
            startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            return
        }

        handler.removeCallbacks(checkForegroundApp)
        handler.post(checkForegroundApp)
    }


    private fun hasUsageAccess(): Boolean {
        val usageStatsManager =
            getSystemService(USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager

        val now = System.currentTimeMillis()
        return try {
            usageStatsManager.queryEvents(now - 60_000, now) != null
        } catch (e: SecurityException) {
            false
        }
    }




    override fun onAccessibilityEvent(
        event: android.view.accessibility.AccessibilityEvent?
    ) {
        event ?: return

        val detectedPackage = event.packageName?.toString() ?: "null"


        if (
            detectedPackage == "com.google.android.youtube" &&
            event.eventType ==
            android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            val source = event.source
            val description = source?.contentDescription?.toString().orEmpty()

            if (
                description.isNotBlank() &&
                !description.startsWith("like this video along with", ignoreCase = true) &&
                description.length > 15 &&
                description != lastShortsDescription

            ) {
                val now = android.os.SystemClock.elapsedRealtime()

                if (now - lastShortsCountTime >= 2000) {
                    lastShortsDescription = description
                    lastShortsCountTime = now

                    val intent = Intent(
                        this,
                        FloatingTimerService::class.java
                    ).apply {
                        action = "com.example.scrollstop.SHORTS_EVENT"
                        putExtra(
                            FloatingTimerService.EXTRA_PACKAGE_NAME,
                            detectedPackage
                        )
                    }

                    startService(intent)

                    android.util.Log.d(
                        "ShortsDebug",
                        "Possible Shorts video change: $description"
                    )
                }
            }
        }


        android.util.Log.d(
            "ScrollStopDetect",
            "ACCESSIBILITY event=${event.eventType}, package=$detectedPackage"
        )

        if (
            event.eventType !=
            android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SCROLLED
        ) {
            return
        }

        if (
            detectedPackage != "com.google.android.youtube" &&
            detectedPackage != "com.instagram.android"
        ) {
            return
        }

        val intent = Intent(
            this,
            FloatingTimerService::class.java
        ).apply {
            action = "com.example.scrollstop.SCROLL_EVENT"
            putExtra(
                FloatingTimerService.EXTRA_PACKAGE_NAME,
                detectedPackage
            )
        }

        startService(intent)

        android.util.Log.d(
            "ScrollStopDetect",
            "Scroll event sent: $detectedPackage"
        )
        android.util.Log.d(
            "ScrollStopDetect",
            "Event name: ${
                android.view.accessibility.AccessibilityEvent
                    .eventTypeToString(event.eventType)
            }"
        )
    }



    override fun onInterrupt() {
        handler.removeCallbacks(checkForegroundApp)
    }

    override fun onDestroy() {
        handler.removeCallbacks(checkForegroundApp)
        super.onDestroy()
    }
}
