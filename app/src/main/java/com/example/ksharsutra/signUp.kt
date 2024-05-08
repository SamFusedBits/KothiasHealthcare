package com.example.ksharsutra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class signUp : AppCompatActivity() {
    lateinit var email: EditText
    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var confirmPassword: EditText
    lateinit var phone: EditText
    lateinit var signUp: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        email = findViewById(R.id.email)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmPassword)
        phone = findViewById(R.id.phone)
        signUp = findViewById(R.id.signup)

        signUp.setOnClickListener() {
            val emailText = email.text.toString()
            val usernameText = username.text.toString()
            val passwordText = password.text.toString()
            val confirmPasswordText = confirmPassword.text.toString()
            val phoneText = phone.text.toString()

            if (emailText.isEmpty() || usernameText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty() || phoneText.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            } else if (passwordText != confirmPasswordText) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
//                Toast.makeText(this, "Email: $emailText, Username: $usernameText, Password: $passwordText, Phone: $phoneText", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, login::class.java)
                startActivity(intent)
            }
        }


    }


}