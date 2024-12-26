package com.naji.prayertimings.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.naji.prayertimings.model.Timing

@Dao
interface TimingDao {

    @Query("SELECT COUNT(*) FROM timing " +
            "WHERE day = :day AND month = :month AND year = :year")
    suspend fun getTimingsCountOfDate(
        day: Int,
        month: Int,
        year: Int
    ): Int

    @Upsert
    suspend fun upsertTimings(timings: List<Timing>)

    @Query("SELECT * FROM timing " +
            "WHERE day = :day AND month = :month AND year = :year")
    fun getTimingsOfDate(
        day: Int,
        month: Int,
        year: Int
    ): LiveData<List<Timing>>
}