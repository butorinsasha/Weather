package com.example.weather

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weather.api.yandex.RetrofitClient
import com.example.weather.api.yandex.RetrofitClient.X_YANDEX_API_KEY
import com.example.weather.api.yandex.YandexWeatherModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WeatherDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_detail)

        val weatherProviderId = intent.getStringExtra("id")

        Toast.makeText(this, "weatherProviderId id is $weatherProviderId", Toast.LENGTH_SHORT).show()

        when (weatherProviderId) {
            "0" -> {
                val call = RetrofitClient.YandexApiClient.yandexWeatherService.getInformers(
                    apiKey = X_YANDEX_API_KEY,
                    lat = 59.938675f,
                    lon = 30.314447f
                )

                call.enqueue(object : Callback<YandexWeatherModel> {
                    override fun onResponse(
                        call: Call<YandexWeatherModel>,
                        response: Response<YandexWeatherModel>
                    ) {
                        if (response.isSuccessful) {
                            val yandexWeatherFact = response.body()?.fact

                            val yandexWeatherTempTextView = findViewById<TextView>(R.id.temp)
                            val yandexWeatherPressureTextView = findViewById<TextView>(R.id.pressure_mm)
                            val yandexWeatherHumidityTextView = findViewById<TextView>(R.id.humidity)

                            yandexWeatherTempTextView.text = yandexWeatherFact?.temp.toString()
                            yandexWeatherPressureTextView.text = yandexWeatherFact?.pressure_mm.toString()
                            yandexWeatherHumidityTextView.text = yandexWeatherFact?.humidity.toString()
                        }
                    }

                    override fun onFailure(call: Call<YandexWeatherModel>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
            }
        }
    }
}