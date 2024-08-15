package com.kothias.clinic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Duration of splash screen (e.g., 2 seconds)
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the main activity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close the splash screen so that user can't go back to it
        }, 800) // Adjust the delay time as needed
    }
}