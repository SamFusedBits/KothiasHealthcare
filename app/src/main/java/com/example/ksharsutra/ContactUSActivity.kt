package com.example.ksharsutra

import android.content.Intent
import android.graphics.Paint
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ContactUSActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contactus)

        val email = findViewById<TextView>(R.id.email)
        val phone = findViewById<TextView>(R.id.phoneno)
        val address = findViewById<TextView>(R.id.address)
        val appointment = findViewById<Button>(R.id.book_an_appointment)
        val back = findViewById<ImageView>(R.id.back_arrow)

        val titleTextView = findViewById<TextView>(R.id.title)

        email.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${getString(R.string.emailID)}")
            }
            startActivity(emailIntent)
        }

        phone.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${getString(R.string.phoneno)}")
            }
            startActivity(dialIntent)
        }

        address.setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=${Uri.encode(getString(R.string.address))}")
            }
            startActivity(mapIntent)
        }
        appointment.setOnClickListener {
            //When the appointment page is ready, then add the binding accordingly
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
        back.setOnClickListener {
            finish()
        }
    }
}