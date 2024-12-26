package com.naji.prayertimings.work_manager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.naji.prayertimings.R
import com.naji.prayertimings.api.APIResource
import com.naji.prayertimings.api.aladhan.AladhanRepository
import com.naji.prayertimings.db.repo.TimingsRepository
import com.naji.prayertimings.model.Timing
import com.naji.prayertimings.model.api.aladhan.AladhanResponse
import com.naji.prayertimings.util.MyUtil
import java.time.Duration
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FetchPrayerTimingWorkManager(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private var day = 0
    private var month = 0
    private var year = 0
    private lateinit var date: String

    override suspend fun doWork(): Result {
        Calendar.getInstance().apply {
            day = get(Calendar.DAY_OF_MONTH)
            month = get(Calendar.MONTH) + 1
            year = get(Calendar.YEAR)
            date = "$day-$month-$year"
        }

        return requestPrayerTimings()
    }

    private suspend fun requestPrayerTimings(): Result {
        MyUtil.getLocationFromPrefs(context)?.let {
            val response = AladhanRepository.getPrayerTimings(date, it)
            if (response is APIResource.Success) {
                val timings = extractTimingsFromResponse(response)
                TimingsRepository.upsertTimings(timings)
                setUpNotifications(timings)
                return Result.success()
            } else
                return Result.failure()
        }
        return Result.failure()
    }

    private fun setUpNotifications(timings: ArrayList<Timing>) {
        for(timing in timings) {
            timing.time.apply {
                val hour = substring(0, indexOf(':')).toInt()
                val minutes = substring(indexOf(':') + 1).toInt()
                val notificationCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minutes)
                    set(Calendar.SECOND, 0)
                }
                val currentCalendar = Calendar.getInstance()
                val millis = notificationCalendar.timeInMillis - currentCalendar.timeInMillis
                startNotificationWorker(timing.name, millis)
            }
        }
    }

    private fun startNotificationWorker(name: String, millis: Long) {
        val workManager = WorkManager.getInstance(context)
        val worker = OneTimeWorkRequestBuilder<PrayerNotificationWorkManager>()
            .setInputData(
                Data.Builder()
                    .putString(PrayerNotificationWorkManager.KEY_PRAYER_NAME, name)
                    .build()
            )
            .setInitialDelay(millis, TimeUnit.MILLISECONDS)
            .build()
        workManager.beginWith(worker).enqueue()
    }

    private fun extractTimingsFromResponse(
        response: APIResource.Success<AladhanResponse>
    ): ArrayList<Timing> {
        val timings = ArrayList<Timing>()
        response.data?.data?.timings?.let {
            timings.add(Timing(context.getString(R.string.fajr), it.Fajr, day, month, year))
            timings.add(Timing(context.getString(R.string.duhur), it.Dhuhr, day, month, year))
            timings.add(Timing(context.getString(R.string.asr), it.Asr, day, month, year))
            timings.add(Timing(context.getString(R.string.maghrib), it.Maghrib, day, month, year))
            timings.add(Timing(context.getString(R.string.isha), it.Isha, day, month, year))
        }
        return timings
    }
}