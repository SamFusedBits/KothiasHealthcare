package com.example.ksharsutra

import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UserDetailsActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        val userDetails: TextView = findViewById(R.id.user_name)

        val nativation_home = findViewById<ImageView>(R.id.navigation_home)
        val nativation_doctor = findViewById<ImageView>(R.id.navigation_doctor)

        val logout = findViewById<Button>(R.id.logout)
        val user = findViewById<TextView>(R.id.user_name)


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

        nativation_doctor.setOnClickListener() {
            val intent = Intent(this, DashboardActivity::class.java)
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