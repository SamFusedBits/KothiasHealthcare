package com.example.ksharsutra

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat

class DashboardActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        val doctorText = findViewById<TextView>(R.id.hello_doctor_text)


        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.search_logo, null)
        val scaledDrawable = BitmapDrawable(resources, Bitmap.createScaledBitmap((drawable as BitmapDrawable).bitmap, 40, 40, true))

        val search_bar = findViewById<EditText>(R.id.search_bar)
        search_bar.setCompoundDrawablesWithIntrinsicBounds(scaledDrawable, null, null, null)

        val download_button1 = findViewById<Button>(R.id.download_button1)
        val download_button2 = findViewById<Button>(R.id.download_button2)
        val download_button3 = findViewById<Button>(R.id.download_button3)

        val navigation_profile = findViewById<ImageView>(R.id.navigation_profile)
        val navigation_home = findViewById<ImageView>(R.id.navigation_home)

        doctorText.text = "${doctorText.text} $username!!!"

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