package com.example.ksharsutra

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {
    lateinit var signup: Button
    lateinit var signin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        signup = findViewById(R.id.signup)
        signin = findViewById(R.id.signin)

        // Set the OnClickListener for the buttons
        signup.setOnClickListener {
            // Start the SignUpActivity
            val intent = Intent(this, signUp::class.java)
            startActivity(intent)
        }

        signin.setOnClickListener {
            // Start the SignInActivity
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
    }
}