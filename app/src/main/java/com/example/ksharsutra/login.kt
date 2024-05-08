package com.example.ksharsutra

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class login : AppCompatActivity() {
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var login: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        login = findViewById(R.id.signin) // Initialize the login Button

        login.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()

            Toast.makeText(this, "Email: $emailText, Password: $passwordText", Toast.LENGTH_SHORT).show()
        }
    }
}