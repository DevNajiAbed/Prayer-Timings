package com.naji.prayertimings.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.naji.prayertimings.db.dao.TimingDao
import com.naji.prayertimings.model.Timing

@Database(
    entities = [
        Timing::class
    ],
    version = 2
)
abstract class PrayerTimingsDb : RoomDatabase() {

    abstract val timingsDao: TimingDao
}