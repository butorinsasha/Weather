package com.example.weather.api.yandex

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient

//openssl s_client -connect api.weather.yandex.ru:443 -servername api.weather.yandex.ru </dev/null 2>/dev/null |
//openssl x509 -pubkey -noout |
//openssl pkey -pubin -outform DER |
//openssl dgst -sha256 -binary |
//openssl base64
//EvDTBD56knrdNlvXhXf7oTrML7CkIX/HVw9yBsq6OLM=

object RetrofitClient {
    private const val YANDEX_WEATHER_BASE_URL = "https://api.weather.yandex.ru/v2/"
    const val X_YANDEX_API_KEY = "22494cd9-231c-488e-a636-028fa5cda9d2"

    val certificatePinner = CertificatePinner.Builder()
        // api.weather.yandex.ru — hostname, для которого действует pin
        .add(
            "api.weather.yandex.ru", // sha256/ — говорим, что это SHA-256 pin
            "sha256/EvDTBD56knrdNlvXhXf7oTrML7CkIX/HVw9yBsq6OLM=" // дальше Base64 хэш публичного ключа
        )
        .build()

    val okHttpClient = OkHttpClient.Builder()
        .certificatePinner(certificatePinner) // Подключаем наш CertificatePinner к OkHttp
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(YANDEX_WEATHER_BASE_URL)
        .client(okHttpClient) // Используем именно наш OkHttpClient
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    object YandexApiClient {
        val yandexWeatherService : YandexWeatherService by lazy {
            retrofit.create(YandexWeatherService::class.java)
        }
    }
}