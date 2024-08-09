package com.example.ksharsutra

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PredictionResponseActivity : AppCompatActivity() {

    // Constants for shared preferences
    private val PREFS_NAME = "PredictionPrefs"
    private val PREDICTION_KEY = "PredictionText"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction_response)

        // Get the prediction text from the intent
        val predictionText = intent.getStringExtra("PREDICTION_TEXT") ?: "Please complete the health assessment to get a prediction."

        // Find the TextView and set the prediction text
        val textViewPrediction: TextView = findViewById(R.id.textViewPrediction)
        textViewPrediction.text = predictionText

        // Cache the prediction text
        cachePrediction(predictionText)

        // Find the button and set an OnClickListener
        val buttonHealthAdvice: Button = findViewById(R.id.buttonHealthAdvice)
        buttonHealthAdvice.setOnClickListener {
            val intent = Intent(this, HealthAdviceActivity::class.java)
            startActivity(intent)
        }
    }

    // Function to cache the prediction text
    private fun cachePrediction(predictionText: String) {
        val sharedPref: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(PREDICTION_KEY, predictionText)
            apply()
        }
    }
}