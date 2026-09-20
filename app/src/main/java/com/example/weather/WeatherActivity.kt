package com.example.weather

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class WeatherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        val weatherListView = findViewById<ListView>(R.id.weather_list)

        val weatherAdapter = ArrayAdapter(
            this,
            R.layout.item_weather_provider,
            R.id.provider_name,
            Weather.providers
        )

        weatherListView.adapter = weatherAdapter

        val itemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                run {
                    val intent = Intent(
                        this,
                        WeatherDetailActivity::class.java
                    )
                    intent.putExtra("id", id.toString())
                    this.startActivity(intent)
                }
            }

        weatherListView.onItemClickListener = itemClickListener
    }
}