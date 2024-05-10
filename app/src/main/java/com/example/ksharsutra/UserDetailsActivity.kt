package com.example.ksharsutra

import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UserDetailsActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        val userDetails: TextView = findViewById(R.id.userDetails)

        val nativation_home = findViewById<ImageView>(R.id.navigation_home)

        val logout = findViewById<ImageView>(R.id.logout)
        val user = findViewById<ImageView>(R.id.user)

        // Get the username, email, and password from SharedPreferences
        val sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        val email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")
        val phonenumber = sharedPreferences.getString("phone_number", "")



        userDetails.text = "Hello $username!!!\nEmail: $email\nPassword: $password\nPhone Number: $phonenumber"

        nativation_home.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        logout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        user.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }
    }
}