package com.example.ksharsutra

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

class HomePageActivity: AppCompatActivity() {
    private lateinit var analytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Obtain the FirebaseAnalytics instance.
        analytics = Firebase.analytics

        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.search_logo, null)
        // Scale the drawable to 40x40 pixels
        val scaledDrawable = BitmapDrawable(resources, Bitmap.createScaledBitmap((drawable as BitmapDrawable).bitmap, 40, 40, true))

        val navigation_profile = findViewById<ImageView>(R.id.navigation_profile)
        val questionnaire_card = findViewById<CardView>(R.id.questionnaire_card)
        val prediction_card = findViewById<CardView>(R.id.analysisscore_card)
        val navigation_report = findViewById<ImageView>(R.id.navigation_report)
        val contactus_card = findViewById<CardView>(R.id.contactus_card)
        val healthadvice_card = findViewById<CardView>(R.id.healthadvice_card)
        val feedback_card = findViewById<CardView>(R.id.feedback_card)
        val aboutus_card = findViewById<CardView>(R.id.aboutus_card)
        val appointment  = findViewById<ImageView>(R.id.navigation_appointment)

        // Handle navigation item clicks
        questionnaire_card.setOnClickListener {
            val intent = Intent(this, QuestionnaireActivity::class.java)
            startActivity(intent)
        }

        navigation_profile.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            startActivity(intent)
        }

        prediction_card.setOnClickListener {
            val intent = Intent(this, PredictionActivity::class.java)
            startActivity(intent)
        }

        navigation_report.setOnClickListener{
            val intent = Intent(this, NavigationReportActivity::class.java)
            startActivity(intent)
        }

        contactus_card.setOnClickListener {
            val intent = Intent(this, ContactUSActivity::class.java)
            startActivity(intent)
        }
        healthadvice_card.setOnClickListener {
            val intent = Intent(this, HealthAdviceActivity::class.java)
            startActivity(intent)
        }
        feedback_card.setOnClickListener() {
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
        }
        aboutus_card.setOnClickListener {
            val intent = Intent(this, AboutUsActivity::class.java)
            startActivity(intent)
        }
        appointment.setOnClickListener {
            val intent = Intent(this, AppointmentActivity::class.java)
            startActivity(intent)
        }
    }
}