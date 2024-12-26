package com.naji.prayertimings.api.aladhan

import com.naji.prayertimings.model.api.aladhan.AladhanResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AladhanAPI {

    @GET("timings/{date}")
    suspend fun getPrayerTimings(
        @Path("date") date: String,
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("method") method: Int = 3
    ) : Response<AladhanResponse>
}