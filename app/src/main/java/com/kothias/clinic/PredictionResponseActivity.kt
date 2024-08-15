package com.kothias.clinic

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
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
        var predictionText = intent.getStringExtra("PREDICTION_TEXT") ?: "Please complete the health assessment to get a prediction."

        // Check if the prediction text matches any of the specified conditions
        if (predictionText in listOf("piles", "fissure", "fistula", "pilonidal sinus")) {
            // Append the required string to the prediction text
            predictionText = "Based on the analysis, it seems you might be at risk of having ${predictionText.uppercase()}"
        } else if (predictionText == "no significant findings") {
            // Convert the prediction text to uppercase
            predictionText = predictionText.uppercase()
        } else {
            // Log a warning message
            Log.w("PredictionResponseActivity", "Invalid prediction text: $predictionText")
        }

        // Find the TextView and set the prediction text
        val textViewPrediction: TextView = findViewById(R.id.textViewPrediction)
        // Create a SpannableString from the prediction text
        val spannableString = SpannableString(predictionText)

        // Find the start and end indices of the prediction part in the prediction text
        val predictionPart = predictionText.substringAfterLast("having ")
        val start = predictionText.indexOf(predictionPart)
        val end = start + predictionPart.length

        // Apply the UnderlineSpan to the prediction part of the prediction text
        if (start >= 0 && end <= spannableString.length) {
            spannableString.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }

        // Set the SpannableString as the text of the TextView
        textViewPrediction.text = spannableString

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