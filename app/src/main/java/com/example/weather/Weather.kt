package com.example.weather

class Weather(private val providerName: String) {
    companion object {
        val providers = arrayListOf(
            Weather("Yandex Weather"),
        )
    }

    override fun toString(): String {
        return this.providerName
    }
}