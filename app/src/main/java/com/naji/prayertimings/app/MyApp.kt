package com.naji.prayertimings.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.naji.prayertimings.R
import com.naji.prayertimings.util.MyUtil.initPrayerTimingsDb
import com.naji.prayertimings.work_manager.FetchPrayerTimingWorkManager
import java.time.Duration
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initPrayerTimingsDb(this)
        initPeriodicPrayerTimingsWorkManager()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                vibrationPattern = longArrayOf(500, 500, 500, 500)
            }
            NotificationManagerCompat.from(this)
                .createNotificationChannel(channel)
        }
    }

    private fun initPeriodicPrayerTimingsWorkManager() {
        val workManager = WorkManager.getInstance(this)
        val worker = PeriodicWorkRequestBuilder<FetchPrayerTimingWorkManager>(
            1, TimeUnit.DAYS
        ).setInitialDelay(calcMillisToMidnight(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "timings_daily_refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            worker
        )
    }

    private fun calcMillisToMidnight(): Long {
        val toMidnightCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val currentCalendar = Calendar.getInstance()
        return toMidnightCalendar.timeInMillis - currentCalendar.timeInMillis
    }

    companion object {
        const val CHANNEL_ID = "channelId"
        const val CHANNEL_NAME = "Prayer timing"
    }
}