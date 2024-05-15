package com.example.ksharsutra

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat

class HomePageActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.search_logo, null)
        val scaledDrawable = BitmapDrawable(resources, Bitmap.createScaledBitmap((drawable as BitmapDrawable).bitmap, 40, 40, true))

        val navigation_profile = findViewById<ImageView>(R.id.navigation_profile)

        val questionnaire_card = findViewById<CardView>(R.id.questionnaire_card)
        val prediction_card = findViewById<CardView>(R.id.analysisscore_card)

        val navigation_report = findViewById<ImageView>(R.id.navigation_report)

        navigation_profile.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            startActivity(intent)
        }

        prediction_card.setOnClickListener {
            val intent = Intent(this, PredictionActivity::class.java)
            startActivity(intent)
        }

        navigation_report.setOnClickListener{
            val intent = Intent(this, PredictionActivity::class.java)
            startActivity(intent)
        }
        }
    }