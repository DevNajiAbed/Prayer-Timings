package com.naji.prayertimings.view_model.activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.naji.prayertimings.db.repo.TimingsRepository
import com.naji.prayertimings.model.Timing
import com.naji.prayertimings.work_manager.FetchPrayerTimingWorkManager
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(
    app: Application
) : AndroidViewModel(app) {

    private var day = 0
    private var month = 0
    private var year = 0

    private val _timingsLiveData = MutableLiveData<List<Timing>>()
    val timingsLiveData: LiveData<List<Timing>> = _timingsLiveData

    init {
        viewModelScope.launch {
            fetchTimingsIfThereAreNot()
            getPrayerTimingsFromDb()
        }
    }

    private suspend fun fetchTimingsIfThereAreNot() {
        Calendar.getInstance().apply {
            day = get(Calendar.DAY_OF_MONTH)
            month = get(Calendar.MONTH) + 1
            year = get(Calendar.YEAR)
            if (!TimingsRepository.doesThisDateHaveTimings(day, month, year))
                startFetchTimingsWorker()
        }
    }

    private fun startFetchTimingsWorker() {
        val workManager = WorkManager.getInstance(getApplication())
        val worker = OneTimeWorkRequestBuilder<FetchPrayerTimingWorkManager>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()
        workManager.beginWith(worker).enqueue()
    }

    private fun getPrayerTimingsFromDb() {
        TimingsRepository.getTimingsOfDate(day, month, year).observeForever {
            _timingsLiveData.value = it
        }
    }
}