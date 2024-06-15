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

        val navigation_home = findViewById<ImageView>(R.id.navigation_home)

        val navigation_report = findViewById<ImageView>(R.id.navigation_report)
        val navigation_appointment = findViewById<ImageView>(R.id.navigation_appointment)
        val logout = findViewById<Button>(R.id.logout)


        // Get the username, email, and password from SharedPreferences
        val sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        val email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")
        val phonenumber = sharedPreferences.getString("phone_number", "")


        userDetails.text = "Hello $username!!!\nEmail: $email\nPassword: $password\nPhone Number: $phonenumber"


        navigation_home.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        navigation_report.setOnClickListener{
            val intent = Intent(this, PredictionActivity::class.java)
            startActivity(intent)
        }

        navigation_appointment.setOnClickListener{
            val intent = Intent(this, AppointmentActivity::class.java)
            startActivity(intent)
        }

        logout.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Clear all activities in the stack and start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()  // Finish UserDetailsActivity to prevent going back to it with back button
        }

        // Navigation to ManageAppointments Page
        val navigation_appointments = findViewById<Button>(R.id.temp_button)
        navigation_appointments.setOnClickListener{
            val intent = Intent(this, ManageAppointmentsActivity::class.java)
            startActivity(intent)
        }
    }
}