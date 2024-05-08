package com.example.ksharsutra

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomePageActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val usernameTextView: TextView = findViewById(R.id.usernameTextView)
        usernameTextView.text = "SamFusedBits" // replace with actual user's name

        val navigation_profile = findViewById<ImageView>(R.id.navigation_profile)

        navigation_profile.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            startActivity(intent)
        }
    }
}