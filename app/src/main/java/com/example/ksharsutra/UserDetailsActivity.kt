package com.example.ksharsutra

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UserDetailsActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        val userDetails: TextView = findViewById(R.id.userDetails)

//        val nativation_home = findViewById<ImageView>(R.id.navigation_home)

        // Get the username, email, and password from SharedPreferences
        val sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        val email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")



        userDetails.text = "Hello $username\nEmail: $email\nPassword: $password"

//        nativation_home.setOnClickListener {
//            val intent = Intent(this, HomePageActivity::class.java)
//            startActivity(intent)
//        }


    }
}