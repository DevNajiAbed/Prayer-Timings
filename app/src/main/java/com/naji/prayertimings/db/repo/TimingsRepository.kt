package com.naji.prayertimings.db.repo

import androidx.lifecycle.LiveData
import com.naji.prayertimings.model.Timing
import com.naji.prayertimings.util.MyUtil

object TimingsRepository {

    private val dao = MyUtil.prayerTimingsDb.timingsDao

    suspend fun doesThisDateHaveTimings(
        day: Int,
        month: Int,
        year: Int
    ): Boolean {
        val count = dao.getTimingsCountOfDate(day, month, year)
        return count > 0
    }

    suspend fun upsertTimings(timings: List<Timing>) {
        dao.upsertTimings(timings)
    }

    fun getTimingsOfDate(
        day: Int,
        month: Int,
        year: Int
    ): LiveData<List<Timing>> = dao.getTimingsOfDate(day, month, year)
}