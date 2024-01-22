package com.example.weather.api.yandex

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface YandexWeatherService {

    @GET("informers/")
    fun getInformers(
        @Header("X-Yandex-API-Key") apiKey: String?,
        @Query("lat") lat: Float,
        @Query("lon") lon: Float
    ): Call<YandexWeatherModel>
}