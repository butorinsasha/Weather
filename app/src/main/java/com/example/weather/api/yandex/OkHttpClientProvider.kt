package com.example.weather.api.yandex

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient

object OkHttpClientProvider {

    val certificatePinner = CertificatePinner.Builder()
        .add(
            "api.weather.yandex.ru",
            "sha256/EvDTBD56knrdNlvXhXf7oTrML7CkIX/HVw9yBsq6OLM="
        )
        .build()

    val client = OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .build()
}