
package com.example.scrollstop

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock.System.now


class FloatingTimerService : Service() {

    private lateinit var windowManager: WindowManager
    private var timerView: TextView? = null

    private var notchView: View? = null
    private var mascotView: TextView? = null




    private val handler = Handler(Looper.getMainLooper())

    private var activeApp = ""
    private var sessionStartTime = 0L
    private var youtubeMillis = 0L
    private var instagramMillis = 0L

    private var todayYoutubeMillis = 0L
    private var todayInstagramMillis = 0L
    private var todayShortsCount = 0
    private var todayReelsCount = 0
    private var currentDate = ""

    private var youtubeScrollCount = 0

    private var instagramScrollCount = 0

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var dailyUsageDao: DailyUsageDao

    private fun restoreTodayUsage() {
        serviceScope.launch {
            val today = java.text.SimpleDateFormat(
                "yyyy-MM-dd",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            currentDate = today



            dailyUsageDao.ensureDailyUsage(
                DailyUsage(date = today)
            )

            dailyUsageDao.getUsageForDate(today).collect { usage ->
                if (usage != null) {
                    todayYoutubeMillis = usage.youtubeMillis
                    todayInstagramMillis = usage.instagramMillis
                    todayShortsCount = usage.shortsCount
                    todayReelsCount = usage.reelsCount

                    youtubeMillis = usage.youtubeMillis
                    instagramMillis = usage.instagramMillis
                    youtubeScrollCount = usage.shortsCount
                    instagramScrollCount = usage.reelsCount

                    if (activeApp.isNotEmpty()) {
                        handler.post {
                            updateTimer()
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_TRACK_APP =
            "com.example.scrollstop.TRACK_APP"
        const val EXTRA_PACKAGE_NAME = "package_name"

        private const val YOUTUBE =
            "com.google.android.youtube"
        private const val INSTAGRAM =
            "com.instagram.android"
    }

    override fun onCreate() {
        super.onCreate()
        dailyUsageDao = ScrollStopDatabase
            .getDatabase(applicationContext)
            .dailyUsageDao()
        restoreTodayUsage()

        handler.postDelayed(checkpointRunnable, 60_000L)

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        val faceView = TextView(this).apply {
            text = getMascot(youtubeScrollCount)
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(10, 0, 6, 0)
        }
        mascotView = faceView

        val view = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(Color.rgb(18, 22, 19))
                setStroke(1, Color.rgb(65, 78, 58))
            }
            elevation = 12f
            setPadding(4, 6, 14, 6)
            addView(
                faceView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val textView = TextView(this@FloatingTimerService).apply {
                text = "YouTube • 00:00 • Shorts: 0"
                textSize = 12f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER_VERTICAL
            }

            addView(
                textView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            visibility = View.GONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 60
        }

        windowManager.addView(view, params)
        notchView = view
        timerView = view.getChildAt(1) as TextView
    }


    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_TRACK_APP -> {
                val detectedPackage =
                    intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""

                switchApp(detectedPackage)
            }
            "com.example.scrollstop.SHORTS_EVENT" -> {
                youtubeScrollCount++
                todayShortsCount++

                val today = java.text.SimpleDateFormat(
                    "yyyy-MM-dd",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())

                serviceScope.launch {
                    dailyUsageDao.ensureDailyUsage(
                        DailyUsage(date = today)
                    )
                    dailyUsageDao.incrementShortsCount(today)
                }

                android.util.Log.d(
                    "ShortsDebug",
                    "Shorts counter increased to: $youtubeScrollCount"
                )

                updateTimer()
            }

            "com.example.scrollstop.SCROLL_EVENT" -> {
                val detectedPackage =
                    intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""

                android.util.Log.d(
                    "ScrollStopCounter",
                    "Received scroll event: $detectedPackage"
                )

                when (detectedPackage) {
                    "com.google.android.youtube" -> {
                        // YouTube Shorts are counted by SHORTS_EVENT instead.
                    }


                    "com.instagram.android" -> {
                        instagramScrollCount++
                        todayReelsCount++

                        val today = java.text.SimpleDateFormat(
                            "yyyy-MM-dd",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date())

                        serviceScope.launch {
                            dailyUsageDao.ensureDailyUsage(
                                DailyUsage(date = today)
                            )
                            dailyUsageDao.incrementReelsCount(today)
                        }
                    }

                }

                updateTimer()
            }
        }

        return START_STICKY
    }



    private fun switchApp(packageName: String) {
        val nextApp = when (packageName) {
            YOUTUBE -> YOUTUBE
            INSTAGRAM -> INSTAGRAM
            else -> ""
        }

        // Ignore duplicate events for the app already being tracked.
        if (nextApp == activeApp) return

        val now = SystemClock.elapsedRealtime()

        // Save elapsed time for the app we are leaving.
        when (activeApp) {
            YOUTUBE -> {
                val elapsed = now - sessionStartTime
                youtubeMillis += elapsed
                todayYoutubeMillis += elapsed

                val today = java.text.SimpleDateFormat(
                    "yyyy-MM-dd",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())

                serviceScope.launch {
                    dailyUsageDao.ensureDailyUsage(
                        DailyUsage(date = today)
                    )
                    dailyUsageDao.addYouTubeTime(today, elapsed)
                }
            }


            INSTAGRAM -> {
                val elapsed = now - sessionStartTime
                instagramMillis += elapsed
                todayInstagramMillis += elapsed

                val today = java.text.SimpleDateFormat(
                    "yyyy-MM-dd",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())

                serviceScope.launch {
                    dailyUsageDao.ensureDailyUsage(
                        DailyUsage(date = today)
                    )
                    dailyUsageDao.addInstagramTime(today, elapsed)
                }
            }

        }

        activeApp = nextApp
        handler.removeCallbacks(timerRunnable)



        if (activeApp.isNotEmpty()) {
            sessionStartTime = now
            notchView?.visibility = View.VISIBLE
            updateTimer()
            handler.postDelayed(timerRunnable, 1000)
        } else {
            notchView?.visibility = View.GONE
        }
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            updateTimer()
            handler.postDelayed(this, 1000)
        }
    }


    private val checkpointRunnable = object : Runnable {
        override fun run() {
            if (activeApp == YOUTUBE || activeApp == INSTAGRAM) {
                val now = SystemClock.elapsedRealtime()
                val elapsed = now - sessionStartTime

                if (elapsed > 0) {
                    when (activeApp) {
                        YOUTUBE -> {
                            youtubeMillis += elapsed
                            todayYoutubeMillis += elapsed
                        }
                        INSTAGRAM -> {
                            instagramMillis += elapsed
                            todayInstagramMillis += elapsed
                        }
                    }

                    sessionStartTime = now

                    val today = java.text.SimpleDateFormat(
                        "yyyy-MM-dd",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())

                    val appToSave = activeApp

                    serviceScope.launch {
                        dailyUsageDao.ensureDailyUsage(
                            DailyUsage(date = today)
                        )

                        when (appToSave) {
                            YOUTUBE -> dailyUsageDao.addYouTubeTime(today, elapsed)
                            INSTAGRAM -> dailyUsageDao.addInstagramTime(today, elapsed)
                        }
                    }
                }
            }

            handler.postDelayed(this, 60_000L)
        }
    }

    private fun getMascot(count: Int): String {
        return when {
            count >= 200 -> "💀"
            count >= 150 -> "🤕"
            count >= 100 -> "😵"
            count >= 50 -> "😟"
            else -> "🙂"
        }
    }


    private fun checkForNewDay() {
        val today = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        if (currentDate.isNotEmpty() && currentDate != today) {
            currentDate = today

            youtubeMillis = 0L
            instagramMillis = 0L

            todayYoutubeMillis = 0L
            todayInstagramMillis = 0L
            todayShortsCount = 0
            todayReelsCount = 0

            youtubeScrollCount = 0
            instagramScrollCount = 0

            serviceScope.launch {
                dailyUsageDao.ensureDailyUsage(
                    DailyUsage(date = today)
                )
            }
        }
    }

    private fun getStressLevel(

        scrollCount: Int,
        sessionMillis: Long
    ): Int {
        val scrollStress = when {
            scrollCount >= 200 -> 5
            scrollCount >= 150 -> 4
            scrollCount >= 100 -> 3
            scrollCount >= 50 -> 2
            else -> 1
        }

        val timeStress = when {
            sessionMillis >= 4 * 60 * 60 * 1000L -> 5
            sessionMillis >= 3 * 60 * 60 * 1000L -> 4
            sessionMillis >= 2 * 60 * 60 * 1000L -> 3
            sessionMillis >= 1 * 60 * 60 * 1000L -> 2
            else -> 1
        }

        return maxOf(scrollStress, timeStress)
    }

    private fun getMascotForStress(level: Int): String {
        return when (level) {
            2 -> "😟"
            3 -> "😵"
            4 -> "🤕"
            5 -> "💀"
            else -> "🙂"
        }
    }











    private fun updateTimer() {
        checkForNewDay()
        val currentSession =
            if (activeApp.isNotEmpty()) {
                SystemClock.elapsedRealtime() - sessionStartTime
            } else {
                0L
            }

        val totalMillis = when (activeApp) {
            YOUTUBE -> youtubeMillis + currentSession
            INSTAGRAM -> instagramMillis + currentSession
            else -> 0L
        }

        val totalSeconds = totalMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val appName = when (activeApp) {
            YOUTUBE -> "YouTube"
            INSTAGRAM -> "Instagram"
            else -> "ScrollStop"
        }


        val scrollCount = when (activeApp) {
            YOUTUBE -> youtubeScrollCount
            INSTAGRAM -> instagramScrollCount
            else -> 0
        }

        val counterLabel = "Scrolls"

        timerView?.text = String.format(
            "%s • %02d:%02d:%02d • %s: %d",
            appName, hours, minutes, seconds,
            counterLabel, scrollCount
        )





        val stressLevel = getStressLevel(scrollCount, currentSession)
        mascotView?.text = getMascotForStress(stressLevel)
    }


    override fun onDestroy() {
        handler.removeCallbacks(timerRunnable)
        handler.removeCallbacks(checkpointRunnable)


        if (activeApp == YOUTUBE || activeApp == INSTAGRAM) {
            val elapsed = SystemClock.elapsedRealtime() - sessionStartTime

            val today = java.text.SimpleDateFormat(
                "yyyy-MM-dd",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            runBlocking(Dispatchers.IO) {
                dailyUsageDao.ensureDailyUsage(
                    DailyUsage(date = today)
                )

                when (activeApp) {
                    YOUTUBE -> dailyUsageDao.addYouTubeTime(today, elapsed)
                    INSTAGRAM -> dailyUsageDao.addInstagramTime(today, elapsed)
                }
            }
        }


        notchView?.let {
            windowManager.removeView(it)
        }
        notchView = null
        timerView = null




        serviceScope.cancel()
        super.onDestroy()
    }


    override fun onBind(intent: Intent?): IBinder? = null
}
