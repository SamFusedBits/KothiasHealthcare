package com.example.ksharsutra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val download_button1 = findViewById<Button>(R.id.download_button1)
        val download_button2 = findViewById<Button>(R.id.download_button2)
        val download_button3 = findViewById<Button>(R.id.download_button3)

        val navigation_profile = findViewById<ImageView>(R.id.navigation_profile)
        val navigation_home = findViewById<ImageView>(R.id.navigation_home)

        navigation_profile.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            startActivity(intent)
        }
        navigation_home.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        download_button1.setOnClickListener(){
            Toast.makeText(this, "Downloading Patient 1 Data", Toast.LENGTH_SHORT).show()
        }
        download_button2.setOnClickListener(){
            Toast.makeText(this, "Downloading Patient 2 Data", Toast.LENGTH_SHORT).show()
        }
        download_button3.setOnClickListener(){
            Toast.makeText(this, "Downloading Patient 3 Data", Toast.LENGTH_SHORT).show()
        }

    }


}