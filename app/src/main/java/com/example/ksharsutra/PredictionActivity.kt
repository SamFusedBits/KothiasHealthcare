package com.example.ksharsutra

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PredictionActivity : AppCompatActivity(){

    private val PREFS_NAME = "PredictionPrefs"
    private val PREDICTION_KEY = "PredictionText"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        val back_arrow = findViewById<ImageView>(R.id.back_arrow)

        back_arrow.setOnClickListener {
            finish()
        }

        // Find the button and set an OnClickListener
        val buttonViewPrediction: Button = findViewById(R.id.view_prediction)
        buttonViewPrediction.setOnClickListener {
            viewPrediction()
        }
    }
    private fun viewPrediction() {
        val sharedPref: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedPrediction = sharedPref.getString(PREDICTION_KEY, null)

        val intent = Intent(this, PredictionResponseActivity::class.java)
        if (cachedPrediction != null) {
            intent.putExtra("PREDICTION_TEXT", cachedPrediction)
        } else {
            intent.putExtra("PREDICTION_TEXT", "Please complete the health assessment to get a prediction.")
        }
        startActivity(intent)
    }
}