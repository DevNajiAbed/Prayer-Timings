package com.naji.prayertimings.util

import android.content.Context
import androidx.room.Room
import com.google.android.gms.maps.model.LatLng
import com.naji.prayertimings.db.PrayerTimingsDb

object MyUtil {

    lateinit var prayerTimingsDb: PrayerTimingsDb
    fun initPrayerTimingsDb(context: Context) {
        prayerTimingsDb = Room.databaseBuilder(
            context,
            PrayerTimingsDb::class.java,
            "prayer_timings_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    fun saveLocationIntoPrefs(context: Context, location: LatLng) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("lat", location.latitude.toString())
            .putString("lng", location.longitude.toString())
            .apply()
    }

    fun getLocationFromPrefs(context: Context): LatLng? {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .apply {
                val latStr = getString("lat", "") ?: return null
                val lngStr = getString("lng", "") ?: return null
                if(latStr.isEmpty())
                    return null
                if(lngStr.isEmpty())
                    return null
                return LatLng(latStr.toDouble(), lngStr.toDouble())
            }
    }
}