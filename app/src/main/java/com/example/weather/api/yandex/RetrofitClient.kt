package com.example.weather.api.yandex

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val YANDEX_WEATHER_BASE_URL = "https://api.weather.yandex.ru/v2/"
    const val X_YANDEX_API_KEY = "22494cd9-231c-488e-a636-028fa5cda9d2"

    val retrofit = Retrofit.Builder()
        .baseUrl(YANDEX_WEATHER_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    object YandexApiClient {
        val yandexWeatherService : YandexWeatherService by lazy {
            retrofit.create(YandexWeatherService::class.java)
        }
    }
}