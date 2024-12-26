package com.naji.prayertimings.work_manager

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naji.prayertimings.R
import com.naji.prayertimings.app.MyApp
import kotlin.random.Random

class PrayerNotificationWorkManager(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        Toast.makeText(context, "HERE", Toast.LENGTH_SHORT).show()
        if(context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
            return Result.failure()

        inputData.apply {
            getString(KEY_PRAYER_NAME)?.let { name ->
                when(name) {
                    "Fajr" -> {
                        startNotification(R.string.fajr)
                    }
                    "Duhur" -> {
                        startNotification(R.string.duhur)
                    }
                    "Asr" -> {
                        startNotification(R.string.asr)
                    }
                    "Maghrib" -> {
                        startNotification(R.string.maghrib)
                    }
                    "Isha" -> {
                        startNotification(R.string.isha)
                    }
                }
            }
        }
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun startNotification(prayerNameId: Int) {
        val notification = NotificationCompat.Builder(context, MyApp.CHANNEL_ID)
            .setContentTitle(context.getString(prayerNameId))
            .setContentText(context.getString(R.string.prayer_time_has_been_come) + context.getString(prayerNameId))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        NotificationManagerCompat.from(context)
            .notify(
                Random.nextInt(),
                notification
            )
    }

    companion object {
        const val KEY_PRAYER_NAME = "key_prayer_name"
    }
}