package com.example.weather

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging


class WeatherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
            }

            val fcmRegistrationToken = task.result

            Log.i("TAG", "FCM token = $fcmRegistrationToken")
        }

        setContentView(R.layout.activity_weather)

        val weatherListView = findViewById<ListView>(R.id.weather_list)

        val weatherAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
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