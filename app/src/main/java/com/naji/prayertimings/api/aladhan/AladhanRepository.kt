package com.naji.prayertimings.api.aladhan

import com.google.android.gms.maps.model.LatLng
import com.naji.prayertimings.api.APIResource
import com.naji.prayertimings.model.api.aladhan.AladhanResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AladhanRepository {

    private val api by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
        Retrofit.Builder()
            .client(client)
            .baseUrl("https://api.aladhan.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AladhanAPI::class.java)
    }

    suspend fun getPrayerTimings(date: String, latlng: LatLng): APIResource<AladhanResponse> {
        val response = api.getPrayerTimings(date, latlng.latitude, latlng.longitude)
        if(response.isSuccessful && response.body() != null)
            return APIResource.Success(response.body()!!)
        return APIResource.Error(response.message())
    }
}