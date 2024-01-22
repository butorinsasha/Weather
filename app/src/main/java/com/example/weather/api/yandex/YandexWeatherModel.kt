package com.example.weather.api.yandex

data class YandexWeatherModel(val fact: Fact?) {

    data class Fact(
        val temp: Float?,
        val pressure_mm: Float?,
        val humidity: Float?
    )
}