package com.example.ksharsutra

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomePageActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val usernameTextView: TextView = findViewById(R.id.usernameTextView)
        usernameTextView.text = "SamFusedBits" // replace with actual user's name
    }
}