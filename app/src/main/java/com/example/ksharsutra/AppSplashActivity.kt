package com.example.ksharsutra

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AppSplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashImage = findViewById<ImageView>(R.id.splash_image)

        // Create an ObjectAnimator that scales the image
        val scaleXAnimator = ObjectAnimator.ofFloat(splashImage, "scaleX", 1.5f)
        val scaleYAnimator = ObjectAnimator.ofFloat(splashImage, "scaleY", 1.5f)

        // Set the duration of the animation to 5 seconds
        scaleXAnimator.duration = 5000
        scaleYAnimator.duration = 5000

        // Start the animations
        scaleXAnimator.start()
        scaleYAnimator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            // Create a SharedPreferences instance
            val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)

            // Retrieve the username and password from SharedPreferences
            val username = sharedPreferences.getString("username", null)
            val password = sharedPreferences.getString("password", null)

            // Check if the username and password are not null
            val intent = if (username != null && password != null) {
                // If they are not null, start HomePageActivity
                Intent(this, HomePageActivity::class.java)
            } else {
                // If they are null, start LoginActivity
                Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, 3000) // delay for 3 seconds (3000 ms)
    }
}