package com.example.weather

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weather.api.yandex.OkHttpClientProvider
import com.example.weather.api.yandex.YandexWeatherModel
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class WeatherDetailActivity : AppCompatActivity() {

    companion object {
        private const val X_YANDEX_API_KEY = "22494cd9-231c-488e-a636-028fa5cda9d2"
        private const val YANDEX_WEATHER_BASE_URL = "https://api.weather.yandex.ru/v2/informers/"

        private const val REQUEST_LOCATION = 100
    }

    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_detail)

        val weatherProviderId = intent.getStringExtra("id")

        Toast.makeText(
            this,
            "weatherProviderId id is $weatherProviderId",
            Toast.LENGTH_SHORT
        ).show()

        locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        when (weatherProviderId) {
            "0" -> requestLocation()
        }
    }

    private fun requestLocation() {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION
            )

            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            10f,
            locationListener
        )
    }

    private val locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            locationManager.removeUpdates(this)

            val latitude = location.latitude
            val longitude = location.longitude

            Toast.makeText(
                this@WeatherDetailActivity,
                "lat=$latitude lon=$longitude",
                Toast.LENGTH_SHORT
            ).show()

            loadWeather(latitude, longitude)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == REQUEST_LOCATION) {
            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                requestLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadWeather(
        latitude: Double,
        longitude: Double
    ) {
        val url = YANDEX_WEATHER_BASE_URL
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("lat", latitude.toString())
            .addQueryParameter("lon", longitude.toString())
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Yandex-API-Key", X_YANDEX_API_KEY)
            .build()

        OkHttpClientProvider.client.newCall(request).enqueue(
            object : okhttp3.Callback {

                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {
                    e.printStackTrace()
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {
                    response.use {
                        if (!response.isSuccessful) {
                            return
                        }

                        val json = response.body?.string()
                            ?: return

                        val weather = Gson().fromJson(
                            json,
                            YandexWeatherModel::class.java
                        )

                        runOnUiThread {
                            findViewById<TextView>(R.id.temp).text = "${weather.fact?.temp} °C"
                            findViewById<TextView>(R.id.pressure_mm).text = "${weather.fact?.pressure_mm} mm"
                            findViewById<TextView>(R.id.humidity).text = "${weather.fact?.humidity} %"
                        }
                    }
                }
            }
        )
    }
}